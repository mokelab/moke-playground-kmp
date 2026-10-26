package com.mokelab.playground.feature.libs.coil

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import coil3.Bitmap
import coil3.transform.Transformation

// Coil 3 の CircleCropTransformation / RoundedCornersTransformation は Android にしか無い
internal actual fun circleCropTransformation(): Transformation = CanvasCircleCropTransformation()

internal actual fun roundedCornersTransformation(radiusPx: Float): Transformation =
    CanvasRoundedCornersTransformation(radiusPx)

internal actual fun Bitmap.toImageBitmap(): ImageBitmap = asComposeImageBitmap()

internal actual fun ImageBitmap.toCoilBitmap(): Bitmap = asSkiaBitmap()
