package com.mokelab.playground

import android.app.Application
import org.koin.android.ext.koin.androidContext

class PlaygroundApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@PlaygroundApplication)
        }
    }
}
