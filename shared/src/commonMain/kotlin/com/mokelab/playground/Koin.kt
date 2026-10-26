package com.mokelab.playground

import com.mokelab.devportal.kmp.devPortalModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(devPortalModule)
        modules(featureModules)
    }
}
