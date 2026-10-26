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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.serviceLoaderEnabled
import coil3.size.Size
import coil3.svg.SvgDecoder
import com.mokelab.playground.core.ui.FeatureScaffold

private const val DEVICON_BASE = "https://cdn.jsdelivr.net/gh/devicons/devicon@v2.16.0/icons"

private val networkSvgUrls = listOf(
    "$DEVICON_BASE/kotlin/kotlin-original.svg",
    "$DEVICON_BASE/android/android-original.svg",
    "$DEVICON_BASE/swift/swift-original.svg",
)

// 24x24 の星。ByteArray を data に渡すと mimeType が無いので、先頭の "<svg" で SVG と判定される
private val starSvg = """
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
      <path fill="#FFB300" stroke="#E65100" stroke-width="0.5"
        d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01z"/>
    </svg>
""".trimIndent().encodeToByteArray()

@Composable
internal fun CoilSvgScreen(
    back: () -> Unit,
) {
    val context = LocalPlatformContext.current
    // coil-svg を依存に入れると SvgDecoder は自動登録される（Android は ServiceLoader、iOS は起動時の初期化）。
    // serviceLoaderEnabled(false) にすると自動登録されたものが外れる（ネットワークの Fetcher も外れる）
    val noSvgLoader = remember(context) {
        ImageLoader.Builder(context)
            .serviceLoaderEnabled(false)
            .build()
    }
    val explicitSvgLoader = remember(context) {
        ImageLoader.Builder(context)
            .serviceLoaderEnabled(false)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    FeatureScaffold(title = "Coil SVG", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("ネットワークの SVG", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                networkSvgUrls.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                    )
                }
            }

            Text("表示サイズとデコード後のサイズ", style = MaterialTheme.typography.titleMedium)
            Text(
                "元の SVG は 24x24。ビットマップは要求サイズで描き直すので、拡大してもぼやけない",
                style = MaterialTheme.typography.bodySmall,
            )
            SvgSizeRow(label = "24dp", displaySize = 24.dp)
            SvgSizeRow(label = "120dp", displaySize = 120.dp)
            // ORIGINAL だと viewBox の大きさ（24x24）でラスタライズされ、表示時に引き伸ばされる
            SvgSizeRow(label = "120dp + Size.ORIGINAL", displaySize = 120.dp, size = Size.ORIGINAL)

            Text("SvgDecoder の登録", style = MaterialTheme.typography.titleMedium)
            SvgLoaderRow(label = "serviceLoaderEnabled(false)", imageLoader = noSvgLoader)
            SvgLoaderRow(
                label = "serviceLoaderEnabled(false)\n+ add(SvgDecoder.Factory())",
                imageLoader = explicitSvgLoader,
            )
        }
    }
}

@Composable
private fun SvgSizeRow(
    label: String,
    displaySize: Dp,
    size: Size? = null,
) {
    var decodedSize by remember { mutableStateOf("Loading") }
    val request = ImageRequest.Builder(LocalPlatformContext.current)
        .data(starSvg)
        // 別のサイズで描いたものがメモリキャッシュから返らないようにする
        .memoryCachePolicy(CachePolicy.DISABLED)
        .apply { if (size != null) size(size) }
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
            modifier = Modifier.size(displaySize)
                .background(Color(0xFFE0E0E0)),
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(decodedSize, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SvgLoaderRow(
    label: String,
    imageLoader: ImageLoader,
) {
    var result by remember { mutableStateOf("Loading") }
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = starSvg,
            contentDescription = null,
            imageLoader = imageLoader,
            onSuccess = { result = "Success" },
            onError = { state -> result = "Error: ${state.result.throwable.message}" },
            modifier = Modifier.size(64.dp)
                .background(Color(0xFFE0E0E0)),
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(result, style = MaterialTheme.typography.bodySmall)
        }
    }
}
