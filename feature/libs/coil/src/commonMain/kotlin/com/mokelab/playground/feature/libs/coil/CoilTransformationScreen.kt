package com.mokelab.playground.feature.libs.coil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.Transformation
import com.mokelab.playground.core.ui.FeatureScaffold

// 横長の画像にして、円形切り抜きで中央が使われることを確認する
private const val SAMPLE_URL = "https://picsum.photos/id/1025/600/400"

private class TransformationSample(
    val label: String,
    val transformations: List<Transformation>,
)

private val samples = listOf(
    TransformationSample("Original", emptyList()),
    TransformationSample("CircleCrop", listOf(circleCropTransformation())),
    TransformationSample("RoundedCorners(48px)", listOf(roundedCornersTransformation(48f))),
    TransformationSample("Grayscale（自作）", listOf(ColorMatrixTransformation.Grayscale)),
    TransformationSample("Sepia（自作）", listOf(ColorMatrixTransformation.Sepia)),
    // リストの順に適用される
    TransformationSample(
        "Grayscale → CircleCrop",
        listOf(ColorMatrixTransformation.Grayscale, circleCropTransformation()),
    ),
)

@Composable
internal fun CoilTransformationScreen(
    back: () -> Unit,
) {
    FeatureScaffold(title = "Coil transformation", back = back) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "CircleCrop / RoundedCorners は Android では Coil 組み込み、" +
                        "iOS では Coil に無いので自作版を使う。灰色の部分は透明",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            items(samples, key = { it.label }) { sample ->
                TransformationCell(sample)
            }
        }
    }
}

@Composable
private fun TransformationCell(sample: TransformationSample) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(sample.label, style = MaterialTheme.typography.labelMedium)
        val request = ImageRequest.Builder(LocalPlatformContext.current)
            .data(SAMPLE_URL)
            // Transformation の cacheKey がメモリキャッシュのキーに入るので、元画像とは別にキャッシュされる
            .transformations(sample.transformations)
            .build()
        AsyncImage(
            model = request,
            contentDescription = null,
            // 変換後の形がわかるように Fit で表示する
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFE0E0E0)),
        )
    }
}
