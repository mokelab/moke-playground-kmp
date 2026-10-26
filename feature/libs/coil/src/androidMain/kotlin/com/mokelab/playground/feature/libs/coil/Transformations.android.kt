package com.mokelab.playground.feature.libs.coil

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Bitmap
import coil3.transform.CircleCropTransformation
import coil3.transform.RoundedCornersTransformation
import coil3.transform.Transformation

internal actual fun circleCropTransformation(): Transformation = CircleCropTransformation()

internal actual fun roundedCornersTransformation(radiusPx: Float): Transformation =
    RoundedCornersTransformation(radiusPx)

// Transformation があるリクエストでは Coil が HARDWARE ではない Bitmap を渡してくるので、Canvas に描ける
internal actual fun Bitmap.toImageBitmap(): ImageBitmap = asImageBitmap()

internal actual fun ImageBitmap.toCoilBitmap(): Bitmap = asAndroidBitmap()
