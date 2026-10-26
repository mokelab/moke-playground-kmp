plugins {
    alias(libs.plugins.mokelab.kmp.library.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DevPortalFeature / DevPortalNavigator / Koin の Module が公開シグネチャに出るので api
            api(libs.devportal.api)
            api(libs.koin.core)
        }
    }
}
