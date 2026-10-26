plugins {
    alias(libs.plugins.mokelab.kmp.library.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.materialIconsCore)
        }
    }
}
