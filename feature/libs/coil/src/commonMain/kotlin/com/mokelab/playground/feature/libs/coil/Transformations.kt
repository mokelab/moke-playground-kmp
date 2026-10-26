package com.mokelab.playground.feature.libs.coil

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import coil3.Bitmap
import coil3.size.Size
import coil3.transform.Transformation

/** 円形に切り抜く。Android は Coil 組み込みの CircleCropTransformation、iOS は Coil に無いので自作版 */
internal expect fun circleCropTransformation(): Transformation

/** 角を丸める。Android は Coil 組み込みの RoundedCornersTransformation、iOS は Coil に無いので自作版 */
internal expect fun roundedCornersTransformation(radiusPx: Float): Transformation

/** coil3.Bitmap（Android: android.graphics.Bitmap / iOS: org.jetbrains.skia.Bitmap）と Compose の ImageBitmap の変換 */
internal expect fun Bitmap.toImageBitmap(): ImageBitmap
internal expect fun ImageBitmap.toCoilBitmap(): Bitmap

/**
 * Compose の [Canvas] で描く [Transformation]。
 * coil3.Bitmap はプラットフォームごとに別の型なので、ImageBitmap に変換して common で描く。
 */
internal abstract class ImageBitmapTransformation : Transformation() {
    abstract fun transformImage(input: ImageBitmap): ImageBitmap

    final override suspend fun transform(input: Bitmap, size: Size): Bitmap =
        transformImage(input.toImageBitmap()).toCoilBitmap()
}

/** [ColorMatrix] で色を変える自作 Transformation */
internal class ColorMatrixTransformation(
    private val matrix: ColorMatrix,
) : ImageBitmapTransformation() {
    // メモリキャッシュのキーに入るので、パラメータ（行列の値）を含める
    override val cacheKey = "ColorMatrixTransformation-${matrix.values.joinToString(",")}"

    override fun transformImage(input: ImageBitmap): ImageBitmap {
        val output = ImageBitmap(input.width, input.height)
        val paint = Paint().apply { colorFilter = ColorFilter.colorMatrix(matrix) }
        Canvas(output).drawImage(input, Offset.Zero, paint)
        return output
    }

    companion object {
        val Grayscale = ColorMatrixTransformation(ColorMatrix().apply { setToSaturation(0f) })

        val Sepia = ColorMatrixTransformation(
            ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            ),
        )
    }
}

/** 自作の円形切り抜き。短い辺を直径にして中央を切り抜く（Coil の CircleCropTransformation と同じ挙動） */
internal class CanvasCircleCropTransformation : ImageBitmapTransformation() {
    override val cacheKey = "CanvasCircleCropTransformation"

    override fun transformImage(input: ImageBitmap): ImageBitmap {
        val minSize = minOf(input.width, input.height)
        val radius = minSize / 2f
        val output = ImageBitmap(minSize, minSize)
        val canvas = Canvas(output)
        // 円を描いてから SrcIn で画像を重ねると、円の部分だけ画像が残る
        canvas.drawCircle(Offset(radius, radius), radius, Paint())
        canvas.drawImage(
            input,
            Offset((minSize - input.width) / 2f, (minSize - input.height) / 2f),
            Paint().apply { blendMode = BlendMode.SrcIn },
        )
        return output
    }
}

/**
 * 自作の角丸。Coil の RoundedCornersTransformation はリクエストのサイズに切り抜いてから丸めるが、
 * こちらは入力画像のサイズのまま丸める。
 */
internal class CanvasRoundedCornersTransformation(
    private val radiusPx: Float,
) : ImageBitmapTransformation() {
    override val cacheKey = "CanvasRoundedCornersTransformation-$radiusPx"

    override fun transformImage(input: ImageBitmap): ImageBitmap {
        val output = ImageBitmap(input.width, input.height)
        val canvas = Canvas(output)
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = input.width.toFloat(),
            bottom = input.height.toFloat(),
            radiusX = radiusPx,
            radiusY = radiusPx,
            paint = Paint(),
        )
        canvas.drawImage(input, Offset.Zero, Paint().apply { blendMode = BlendMode.SrcIn })
        return output
    }
}
