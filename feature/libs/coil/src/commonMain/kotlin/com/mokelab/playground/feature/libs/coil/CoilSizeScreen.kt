package com.mokelab.playground.feature.libs.coil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import com.mokelab.playground.core.ui.FeatureScaffold

// 元画像は 1200x800
private const val SAMPLE_URL = "https://picsum.photos/id/1015/1200/800"

private class SizeSample(
    val label: String,
    val configure: ImageRequest.Builder.() -> ImageRequest.Builder,
)

private val samples = listOf(
    // size を指定しないと AsyncImage の大きさ（ここでは 120dp）から決まる
    SizeSample("指定なし（120dp の表示サイズ）") { this },
    SizeSample("Size.ORIGINAL") { size(Size.ORIGINAL) },
    // FIT: 200x200 に収まるように縮小する
    SizeSample("size(200) + Scale.FIT") { size(200).scale(Scale.FIT) },
    // FILL: 200x200 を埋めるように縮小する（はみ出した部分は切らない）
    SizeSample("size(200) + Scale.FILL") { size(200).scale(Scale.FILL) },
    // INEXACT: 2 のべき乗のサンプリングだけで済ませ、指定より大きいまま返すことがある
    SizeSample("size(200) + FIT + INEXACT") {
        size(200).scale(Scale.FIT).precision(Precision.INEXACT)
    },
    SizeSample("size(width = 100, height = 300) + FILL") { size(100, 300).scale(Scale.FILL) },
)

@Composable
internal fun CoilSizeScreen(
    back: () -> Unit,
) {
    FeatureScaffold(title = "Coil size / scale", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "元画像は 1200x800。右側はデコード後の Bitmap のサイズ（px）",
                style = MaterialTheme.typography.bodySmall,
            )
            samples.forEach { sample ->
                SizeRow(sample)
            }
        }
    }
}

@Composable
private fun SizeRow(sample: SizeSample) {
    var decodedSize by remember { mutableStateOf("Loading") }
    val request = ImageRequest.Builder(LocalPlatformContext.current)
        .data(SAMPLE_URL)
        // 大きいサイズのキャッシュがあると小さい指定でもそれが使われるので、メモリキャッシュは無効にする
        .memoryCachePolicy(CachePolicy.DISABLED)
        .let(sample.configure)
        .build()
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val image = state.result.image
                decodedSize = "${image.width} x ${image.height}"
            },
            onError = { decodedSize = "Error" },
            modifier = Modifier.size(120.dp)
                .background(Color(0xFFE0E0E0)),
        )
        Column {
            Text(sample.label, style = MaterialTheme.typography.labelMedium)
            Text(decodedSize, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
