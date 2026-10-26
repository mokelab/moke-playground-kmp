package com.mokelab.playground.feature.libs.coil

import android.os.Build
import coil3.DrawableImage
import coil3.Image
import coil3.decode.Decoder
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.size.ScaleDrawable

// coil-gif は依存に入れるだけで ServiceLoader 経由で自動登録される（coil-svg と同じ）。
// 自動登録を外した ImageLoader に明示的に足すときに使う。
// API 28 以上は ImageDecoder（AnimatedImageDrawable）、それ未満は Movie を使う GifDecoder
internal actual fun animatedImageDecoderFactory(): Decoder.Factory =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        AnimatedImageDecoder.Factory()
    } else {
        GifDecoder.Factory()
    }

internal actual fun describeImage(image: Image): String = when (image) {
    // AnimatedImageDecoder は AnimatedImageDrawable を coil3.size.ScaleDrawable で包んで返す
    is DrawableImage -> {
        val drawable = image.drawable
        val inner = (drawable as? ScaleDrawable)?.child ?: drawable
        "DrawableImage (${inner::class.simpleName})"
    }
    else -> image::class.simpleName ?: "Unknown"
}
