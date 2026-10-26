package com.mokelab.playground.feature.libs.coil

import coil3.PlatformContext
import org.koin.core.scope.Scope

// Android の PlatformContext は Context の typealias。PlaygroundApplication の androidContext() で登録済み
internal actual fun Scope.platformContext(): PlatformContext = get()
