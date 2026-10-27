package com.mokelab.playground.feature.libs.coil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.EventListener
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.decode.DataSource
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.mokelab.playground.core.ui.FeatureScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val imageUrls = List(100) { "https://picsum.photos/seed/moke$it/300/300" }

/** 画像の取得元。null は読み込み中 */
private sealed interface LoadSource {
    data class Success(val dataSource: DataSource) : LoadSource
    data object Error : LoadSource
}

@Composable
internal fun CoilCacheScreen(
    back: () -> Unit,
) {
    val context = LocalPlatformContext.current
    // URL -> 取得元。EventListener から書き込む
    val sources = remember { mutableStateMapOf<String, LoadSource>() }
    // キャッシュクリア後に AsyncImage を作り直して読み込み直すためのキー
    var generation by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // EventListener は ImageLoader 単位なので、この画面専用の ImageLoader を作る
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .eventListener(object : EventListener() {
                override fun onStart(request: ImageRequest) {
                    sources.remove(request.data.toString())
                }

                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    sources[request.data.toString()] = LoadSource.Success(result.dataSource)
                }

                override fun onError(request: ImageRequest, result: ErrorResult) {
                    sources[request.data.toString()] = LoadSource.Error
                }
            })
            .build()
    }

    FeatureScaffold(title = "Coil cache", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = {
                    scope.launch {
                        imageLoader.memoryCache?.clear()
                        // ディスクキャッシュの削除はファイル操作なので IO で行う
                        withContext(Dispatchers.IO) {
                            imageLoader.diskCache?.clear()
                        }
                        sources.clear()
                        generation++
                    }
                }) {
                    Text("Clear cache")
                }
                SourceSummary(sources.values)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(imageUrls, key = { "$generation-$it" }) { url ->
                    ImageCell(
                        url = url,
                        source = sources[url],
                        imageLoader = imageLoader,
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceSummary(sources: Collection<LoadSource>) {
    val counts = sources.groupingBy { it }.eachCount()
    Column {
        Text(
            "Memory: ${counts[LoadSource.Success(DataSource.MEMORY_CACHE)] ?: 0}" +
                " / Disk: ${counts[LoadSource.Success(DataSource.DISK)] ?: 0}" +
                " / Network: ${counts[LoadSource.Success(DataSource.NETWORK)] ?: 0}",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            "Error: ${counts[LoadSource.Error] ?: 0}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ImageCell(
    url: String,
    source: LoadSource?,
    imageLoader: ImageLoader,
) {
    Box(
        modifier = Modifier.aspectRatio(1f)
            .padding(1.dp),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        val (label, color) = source.label()
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomStart)
                .background(color)
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}

private fun LoadSource?.label(): Pair<String, Color> = when (this) {
    null -> "Loading" to Color.Gray
    LoadSource.Error -> "Error" to Color(0xFFC62828)
    is LoadSource.Success -> when (dataSource) {
        DataSource.MEMORY_CACHE -> "Memory" to Color(0xFF2E7D32)
        DataSource.MEMORY -> "Memory(data)" to Color(0xFF558B2F)
        DataSource.DISK -> "Disk" to Color(0xFF1565C0)
        DataSource.NETWORK -> "Network" to Color(0xFFEF6C00)
    }
}
