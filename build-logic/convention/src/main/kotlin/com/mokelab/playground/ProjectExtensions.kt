package com.mokelab.playground

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

internal fun VersionCatalog.int(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()

/** `:feature:m3:calendar` → `com.mokelab.playground.feature.m3.calendar` */
internal val Project.defaultNamespace: String
    get() = "com.mokelab.playground" + path.replace(':', '.')
