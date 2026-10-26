plugins {
    alias(libs.plugins.mokelab.kmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.coil.svg)
            implementation(libs.koin.compose)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
