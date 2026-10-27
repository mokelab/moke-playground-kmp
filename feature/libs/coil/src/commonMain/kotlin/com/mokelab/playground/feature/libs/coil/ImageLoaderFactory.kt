package com.mokelab.playground.feature.libs.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import okio.FileSystem
import org.koin.core.scope.Scope

/** Koin から [ImageLoader] を作るときに使う。Android は androidContext()、iOS は PlatformContext.INSTANCE */
internal expect fun Scope.platformContext(): PlatformContext

internal const val MEMORY_CACHE_PERCENT = 0.1
internal const val DISK_CACHE_MAX_BYTES = 50L * 1024 * 1024

/**
 * キャッシュサイズとログを設定した [ImageLoader] を作る。
 * setSingletonImageLoaderFactory で渡していたものと同じだが、Koin の single として持つ。
 */
internal fun newImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .memoryCache {
            // アプリが使えるメモリの 10%（既定は 15〜25%）
            MemoryCache.Builder()
                .maxSizePercent(context, MEMORY_CACHE_PERCENT)
                .build()
        }
        .diskCache {
            // Android は java.io.tmpdir = cacheDir、iOS は NSTemporaryDirectory になる
            DiskCache.Builder()
                .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "koin_image_cache")
                .maxSizeBytes(DISK_CACHE_MAX_BYTES)
                .build()
        }
        // Android は Logcat（tag: ImageLoader 等）、iOS は println で Xcode のコンソールに出る
        .logger(DebugLogger())
        .build()
