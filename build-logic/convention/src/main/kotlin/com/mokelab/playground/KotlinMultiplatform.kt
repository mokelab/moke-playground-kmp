package com.mokelab.playground

import com.android.build.api.dsl.androidLibrary
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Android + iOS(arm64 / simulatorArm64) の KMP ライブラリ共通設定 */
internal fun Project.configureKmpLibrary() {
    extensions.configure<KotlinMultiplatformExtension> {
        androidLibrary {
            namespace = defaultNamespace
            compileSdk = libs.int("android-compileSdk")
            minSdk = libs.int("android-minSdk")
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
        iosArm64()
        iosSimulatorArm64()

        sourceSets.getByName("commonMain").dependencies {
            implementation(libs.lib("compose-runtime"))
            implementation(libs.lib("compose-foundation"))
            implementation(libs.lib("compose-ui"))
            implementation(libs.lib("compose-material3"))
        }
    }
}
