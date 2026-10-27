package com.mokelab.playground.feature.libs.coil

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.mokelab.playground.core.ui.FeatureScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

private val imageUrls = List(60) { "https://picsum.photos/seed/koin$it/300/300" }

@Composable
internal fun CoilKoinScreen(
    back: () -> Unit,
    // Koin の single なので、どの画面から取っても同じインスタンス（= キャッシュも共有）
    imageLoader: ImageLoader = koinInject(),
) {
    var memorySize by remember { mutableLongStateOf(0) }
    var diskSize by remember { mutableLongStateOf(0) }
    var generation by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // キャッシュの使用量を 1 秒ごとに読み直す
    LaunchedEffect(imageLoader) {
        while (true) {
            memorySize = imageLoader.memoryCache?.size ?: 0
            diskSize = withContext(Dispatchers.IO) { imageLoader.diskCache?.size ?: 0 }
            delay(1000)
        }
    }

    FeatureScaffold(title = "Coil ImageLoader (Koin)", back = back) { innerPadding ->
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
                        withContext(Dispatchers.IO) {
                            imageLoader.diskCache?.clear()
                        }
                        generation++
                    }
                }) {
                    Text("Clear cache")
                }
                Column {
                    Text(
                        "Memory: ${memorySize.toMegaBytes()} / " +
                            "${(imageLoader.memoryCache?.maxSize ?: 0).toMegaBytes()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Disk: ${diskSize.toMegaBytes()} / " +
                            "${(imageLoader.diskCache?.maxSize ?: 0).toMegaBytes()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                "DebugLogger のログは Logcat / Xcode のコンソールに出る",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(imageUrls, key = { "$generation-$it" }) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        imageLoader = imageLoader,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f)
                            .padding(1.dp),
                    )
                }
            }
        }
    }
}

private fun Long.toMegaBytes(): String {
    val tenths = this * 10 / (1024 * 1024)
    return "${tenths / 10}.${tenths % 10} MB"
}
