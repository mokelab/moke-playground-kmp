import com.mokelab.playground.lib
import com.mokelab.playground.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * feature/ 以下のモジュール用。
 * KMP + Compose に加えて :core:devportal / :core:ui / Koin を commonMain に入れる。
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("mokelab.kmp.library.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:devportal"))
                    implementation(project(":core:ui"))
                    implementation(libs.lib("koin-core"))
                }
            }
        }
    }
}
