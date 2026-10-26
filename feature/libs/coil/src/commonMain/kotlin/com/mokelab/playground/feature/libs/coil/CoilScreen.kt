package com.mokelab.playground.feature.libs.coil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mokelab.playground.core.ui.FeatureScaffold

private const val SAMPLE_URL = "https://picsum.photos/id/237/600/400"

@Composable
internal fun CoilScreen(
    back: () -> Unit,
) {
    FeatureScaffold(title = "Coil", back = back) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text("AsyncImage")
            // ネットワークの Fetcher は coil-network-ktor3 を依存に入れるだけで自動登録される
            AsyncImage(
                model = SAMPLE_URL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
                    .height(240.dp),
            )
        }
    }
}
