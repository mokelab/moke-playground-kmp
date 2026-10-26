enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://mokelab.github.io/devportal-kmp/repo")
        }
    }
}

rootProject.name = "MokePlaygroundKMP"
include(":androidApp")
include(":shared")
include(":core:devportal")
include(":core:ui")

// 試す feature モジュールは local.properties で指定する。
//   features=m3:calendar,compose:foo
// "*" を指定すると feature/ 以下の全モジュールを有効にする。
val featureRoot: File = settingsDir.resolve("feature")
val requestedFeatures: List<String> = java.util.Properties().run {
    val file = settingsDir.resolve("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
    getProperty("features").orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
val enabledFeatures: List<String> = if (requestedFeatures == listOf("*")) {
    featureRoot.walkTopDown()
        .filter { it.name == "build.gradle.kts" }
        .map { it.parentFile.relativeTo(featureRoot).invariantSeparatorsPath.replace('/', ':') }
        .sorted()
        .toList()
} else {
    requestedFeatures
}
enabledFeatures.forEach { name ->
    val dir = featureRoot.resolve(name.replace(':', '/'))
    require(dir.resolve("build.gradle.kts").exists()) {
        "local.properties: feature '$name' listed in 'features' was not found ($dir)"
    }
    include(":feature:$name")
}
gradle.extra["playground.features"] = enabledFeatures
