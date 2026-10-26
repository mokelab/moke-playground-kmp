plugins {
    `kotlin-dsl`
}

group = "com.mokelab.playground.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // 実装はルートの build.gradle.kts の `apply false` 経由で classpath に載る
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibraryCompose") {
            id = "mokelab.kmp.library.compose"
            implementationClass = "KmpLibraryComposeConventionPlugin"
        }
        register("kmpFeature") {
            id = "mokelab.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
    }
}
