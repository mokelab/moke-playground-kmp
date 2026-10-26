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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import com.mokelab.playground.core.ui.FeatureScaffold

private val gifUrls = listOf(
    "https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif",
    "https://upload.wikimedia.org/wikipedia/commons/d/d3/Newtons_cradle_animation_book_2.gif",
)

@Composable
internal fun CoilGifScreen(
    back: () -> Unit,
) {
    val context = LocalPlatformContext.current
    // ImageLoader ごとにメモリキャッシュが別なので、別の Decoder でデコードした結果が混ざらない
    val defaultLoader = remember(context) { ImageLoader.Builder(context).build() }
    // serviceLoaderEnabled(false) だとネットワークの Fetcher も外れるので、自分で足す
    val noGifLoader = remember(context) {
        ImageLoader.Builder(context)
            .serviceLoaderEnabled(false)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    val animatedLoader = remember(context) {
        ImageLoader.Builder(context)
            .serviceLoaderEnabled(false)
            .components {
                add(KtorNetworkFetcherFactory())
                add(animatedImageDecoderFactory())
            }
            .build()
    }

    FeatureScaffold(title = "Coil GIF", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("既定の ImageLoader", style = MaterialTheme.typography.titleMedium)
            Text(
                "Android は coil-gif が自動登録されるので動く。iOS は coil-gif が無いので静止画",
                style = MaterialTheme.typography.bodySmall,
            )
            GifRow(imageLoader = defaultLoader)

            Text("serviceLoaderEnabled(false)", style = MaterialTheme.typography.titleMedium)
            Text(
                "GIF 用の Decoder が無いので、1 フレーム目だけの静止画になる",
                style = MaterialTheme.typography.bodySmall,
            )
            GifRow(imageLoader = noGifLoader)

            Text(
                "serviceLoaderEnabled(false)\n+ add(animatedImageDecoderFactory())",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Android は coil-gif、iOS は Skia の Codec を使った自作の Decoder",
                style = MaterialTheme.typography.bodySmall,
            )
            GifRow(imageLoader = animatedLoader)
        }
    }
}

@Composable
private fun GifRow(
    imageLoader: ImageLoader,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        gifUrls.forEach { url ->
            var result by remember { mutableStateOf("Loading") }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    imageLoader = imageLoader,
                    onSuccess = { state -> result = describeImage(state.result.image) },
                    onError = { state -> result = "Error: ${state.result.throwable.message}" },
                    modifier = Modifier.size(140.dp)
                        .background(Color(0xFFE0E0E0)),
                )
                Text(result, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
