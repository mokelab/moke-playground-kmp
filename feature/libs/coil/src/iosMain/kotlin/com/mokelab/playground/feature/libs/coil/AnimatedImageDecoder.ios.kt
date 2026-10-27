package com.mokelab.playground.feature.libs.coil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import coil3.Canvas
import coil3.Image
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encodeUtf8
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Canvas as SkiaCanvas

internal actual fun animatedImageDecoderFactory(): Decoder.Factory = SkiaAnimatedImageDecoder.Factory()

internal actual fun describeImage(image: Image): String = when (image) {
    is AnimatedSkiaImage -> "AnimatedSkiaImage (${image.frameCount} frames)"
    else -> image::class.simpleName ?: "Unknown"
}

// GIF87a / GIF89a
private val GIF_HEADER = "GIF8".encodeUtf8()

// ブラウザと同じく、0〜10ms の指定は 100ms として扱う
private const val MIN_FRAME_DURATION_MILLIS = 20L
private const val DEFAULT_FRAME_DURATION_MILLIS = 100L

/**
 * Skia の [Codec] で GIF の全フレームをデコードする。
 * Coil 標準の SkiaImageDecoder は Image.makeFromEncoded で 1 フレーム目しか取り出さない。
 */
internal class SkiaAnimatedImageDecoder(
    private val source: ImageSource,
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val bytes = source.source().use { it.readByteArray() }
        val data = Data.makeFromBytes(bytes)
        val codec = Codec.makeFromData(data)
        try {
            val info = codec.imageInfo
            val framesInfo = codec.framesInfo
            val bitmaps = ArrayList<Bitmap>(codec.frameCount)
            for (index in 0 until codec.frameCount) {
                bitmaps += decodeFrame(codec, info, index, framesInfo[index].requiredFrame, bitmaps)
            }
            if (bitmaps.size <= 1) {
                return DecodeResult(image = bitmaps.first().asImage(), isSampled = false)
            }
            val durations = framesInfo.map { frame ->
                frame.duration.toLong().let { if (it <= 10L) DEFAULT_FRAME_DURATION_MILLIS else it }
                    .coerceAtLeast(MIN_FRAME_DURATION_MILLIS)
            }
            return DecodeResult(
                image = AnimatedSkiaImage(
                    frames = bitmaps.map { SkiaImage.makeFromBitmap(it) },
                    durationsMillis = durations,
                    repetitionCount = codec.repetitionCount,
                ),
                // 要求サイズへの縮小はしていない（元の大きさで全フレームを持つ）
                isSampled = false,
            )
        } finally {
            codec.close()
            data.close()
        }
    }

    private fun decodeFrame(
        codec: Codec,
        info: ImageInfo,
        index: Int,
        requiredFrame: Int,
        decoded: List<Bitmap>,
    ): Bitmap {
        val bitmap = Bitmap()
        check(bitmap.allocPixelsFlags(info, zeroPixels = true)) { "allocPixels($info) failed" }
        if (requiredFrame < 0) {
            // 前のフレームに依存しない（画面全体を描き直す）フレーム
            codec.readPixels(bitmap, index)
        } else {
            // GIF は前のフレームとの差分しか持たないことが多い。
            // 依存先のフレームをコピーしておき、priorFrame で伝えると差分だけデコードしてくれる。
            // makeClone() はピクセルを共有してしまうので writePixels でコピーする
            val canvas = SkiaCanvas(bitmap)
            try {
                canvas.writePixels(decoded[requiredFrame], 0, 0)
            } finally {
                canvas.close()
            }
            codec.readPixels(bitmap, index, requiredFrame)
        }
        return bitmap.setImmutable()
    }

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            // null を返すと次の Decoder（標準の SkiaImageDecoder）に回る
            if (!result.source.source().rangeEquals(0, GIF_HEADER)) return null
            return SkiaAnimatedImageDecoder(result.source)
        }
    }
}

// フレームを進めるコルーチンはメインスレッドで動かす（draw もメインスレッドで呼ばれる）
private val animationScope = MainScope()

// この回数だけフレームを進めても描画されなかったら、画面から消えたとみなして止める
private const val MAX_IDLE_TICKS = 3

/**
 * 全フレームを持ち、時間に合わせて描くフレームを切り替える [Image]。
 *
 * Coil の ImagePainter は image.draw() を呼ぶだけで、描き直しを頼む仕組みが無い。
 * そこで今のフレーム番号を Compose の State に持ち、draw() の中で読む。
 * 描画中に読んだ State は Compose が監視しているので、番号を変えると描き直される。
 */
internal class AnimatedSkiaImage(
    private val frames: List<SkiaImage>,
    private val durationsMillis: List<Long>,
    private val repetitionCount: Int,
) : Image {

    val frameCount: Int get() = frames.size

    override val size: Long = frames.sumOf { 4L * it.width * it.height }

    override val width: Int = frames.first().width

    override val height: Int = frames.first().height

    // メモリキャッシュから同じインスタンスが複数の AsyncImage に渡ってもよい（同じ動きをするだけ）
    override val shareable: Boolean = true

    private var frameIndex by mutableIntStateOf(0)
    private var drawnSinceLastTick = false
    private var finished = false
    private var job: Job? = null

    override fun draw(canvas: Canvas) {
        val frame = frames[frameIndex]
        drawnSinceLastTick = true
        startIfNeeded()
        canvas.drawImage(frame, 0f, 0f)
    }

    private fun startIfNeeded() {
        if (finished || job?.isActive == true) return
        job = animationScope.launch {
            var idleTicks = 0
            var plays = 1
            while (true) {
                delay(durationsMillis[frameIndex])
                if (drawnSinceLastTick) {
                    idleTicks = 0
                } else if (++idleTicks >= MAX_IDLE_TICKS) {
                    // 次に draw() されたら再開する
                    break
                }
                drawnSinceLastTick = false

                val next = frameIndex + 1
                if (next < frames.size) {
                    frameIndex = next
                } else if (repetitionCount < 0 || plays <= repetitionCount) {
                    // repetitionCount は 1 回目を含まない繰り返し回数。負なら無限
                    plays++
                    frameIndex = 0
                } else {
                    // 最後のフレームで止める
                    finished = true
                    break
                }
            }
        }
    }
}
