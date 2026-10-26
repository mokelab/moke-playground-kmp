package com.mokelab.playground.core.devportal

import androidx.compose.runtime.Composable
import com.mokelab.devportal.kmp.api.DevPortalFeature
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * [DevPortalFeature] を Koin に登録する。
 *
 * Koin は同じ型・同じ qualifier の定義を後勝ちで上書きするので、
 * `single<DevPortalFeature> { ... }` を qualifier なしで複数書くと 1 つしか残らない。
 * ここでは [id] を qualifier にして、`getAll<DevPortalFeature>()` で全部取れるようにしている。
 */
fun Module.devPortalFeature(id: String, factory: Scope.() -> DevPortalFeature) {
    single<DevPortalFeature>(named("devportal.feature.$id")) { factory() }
}

/** 画面が 1 つだけの feature を登録する。[singleScreenFeature] の Koin 版 */
fun Module.singleScreenFeature(
    name: String,
    id: String = name,
    screen: @Composable (back: () -> Unit) -> Unit,
) {
    devPortalFeature(id) { singleScreenFeature(name, get(), id, screen) }
}
