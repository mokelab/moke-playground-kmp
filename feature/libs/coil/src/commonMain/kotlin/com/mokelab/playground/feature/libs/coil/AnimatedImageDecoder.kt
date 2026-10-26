package com.mokelab.playground.feature.libs.coil

import coil3.Image
import coil3.decode.Decoder

/**
 * GIF をアニメーションとしてデコードする [Decoder.Factory]。
 * Android は coil-gif、iOS は coil-gif が無いので Skia の Codec を使った自作のもの。
 */
internal expect fun animatedImageDecoderFactory(): Decoder.Factory

/** デコード結果がどの [Image] になったかを画面に出すための説明 */
internal expect fun describeImage(image: Image): String
