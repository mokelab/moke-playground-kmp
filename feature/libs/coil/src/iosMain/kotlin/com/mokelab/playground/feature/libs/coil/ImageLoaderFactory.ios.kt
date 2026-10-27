package com.mokelab.playground.feature.libs.coil

import coil3.PlatformContext
import org.koin.core.scope.Scope

internal actual fun Scope.platformContext(): PlatformContext = PlatformContext.INSTANCE
