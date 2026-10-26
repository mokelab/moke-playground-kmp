package com.mokelab.playground.core.devportal

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import com.mokelab.devportal.kmp.api.DevPortalFeature
import com.mokelab.devportal.kmp.api.DevPortalNavigator

/**
 * 単一画面 feature 用の navigation3 キー。
 *
 * クラスキー(entry<T>)ではなくインスタンスキー(entry(key))として使うため、
 * 全 feature がこの 1 つの型を共有しても衝突しない。
 */
data class FeatureKey(val id: String)

private class SingleScreenFeature(
    override val name: String,
    private val key: FeatureKey,
    private val navigator: DevPortalNavigator,
    private val screen: @Composable (back: () -> Unit) -> Unit,
) : DevPortalFeature {
    override val root: Any get() = key

    override val installer: EntryProviderScope<Any>.() -> Unit = {
        entry(key) { screen { navigator.goBack() } }
    }
}

/**
 * 画面が 1 つだけの [DevPortalFeature] を作る。
 *
 * 複数画面を持つ feature はこのヘルパーを使わず、自前のキー型と `entry<T>` を書くこと。
 *
 * @param name DevPortal のメニューに表示される名前
 * @param navigator 戻る操作に使う
 * @param id navigation3 のキー id。既定は [name]。他 feature と重複すると起動時に例外になる
 * @param screen back ラムダを受け取る画面 Composable
 */
fun singleScreenFeature(
    name: String,
    navigator: DevPortalNavigator,
    id: String = name,
    screen: @Composable (back: () -> Unit) -> Unit,
): DevPortalFeature = SingleScreenFeature(name, FeatureKey(id), navigator, screen)
