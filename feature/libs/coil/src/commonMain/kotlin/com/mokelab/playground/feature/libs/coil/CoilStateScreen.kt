package com.mokelab.playground.feature.libs.coil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mokelab.playground.core.ui.FeatureScaffold

// 存在しないホストなので必ず失敗する
private const val ERROR_URL = "https://example.invalid/image.png"

private val placeholderPainter = ColorPainter(Color(0xFF90CAF9))
private val errorPainter = ColorPainter(Color(0xFFEF9A9A))
private val fallbackPainter = ColorPainter(Color(0xFFFFE082))

@Composable
internal fun CoilStateScreen(
    back: () -> Unit,
) {
    // ボタンで URL を変えて読み込み直す
    var reloadCount by remember { mutableIntStateOf(0) }
    val imageUrl = "https://picsum.photos/seed/moke$reloadCount/600/400"

    FeatureScaffold(title = "Coil placeholder / error", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { reloadCount++ }) {
                Text("Reload")
            }

            Text("placeholder（青）: 読み込み中に表示")
            SampleImage(model = imageUrl, crossfade = false)

            Text("crossfade(1000): placeholder から 1 秒かけて切り替え")
            SampleImage(model = imageUrl, crossfade = true)

            Text("error（赤）: 読み込みに失敗したときに表示")
            SampleImage(model = ERROR_URL, crossfade = false)

            Text("fallback（黄）: data が null のときに表示")
            SampleImage(model = null, crossfade = false)
        }
    }
}

@Composable
private fun SampleImage(
    model: String?,
    crossfade: Boolean,
) {
    val request = ImageRequest.Builder(LocalPlatformContext.current)
        .data(model)
        // キャッシュに当たると placeholder / crossfade が見えないので無効にする
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .crossfade(if (crossfade) 1000 else 0)
        .build()
    AsyncImage(
        model = request,
        contentDescription = null,
        placeholder = placeholderPainter,
        error = errorPainter,
        // fallback を省略すると data が null のときも error が使われる
        fallback = fallbackPainter,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth()
            .height(200.dp),
    )
}
