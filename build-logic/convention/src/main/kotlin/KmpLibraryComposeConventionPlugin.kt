import com.mokelab.playground.configureKmpLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Compose Multiplatform を使う KMP ライブラリ (Android + iOS)。
 * namespace はプロジェクトパスから自動で決まる。
 */
class KmpLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target.pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }
        target.configureKmpLibrary()
    }
}
