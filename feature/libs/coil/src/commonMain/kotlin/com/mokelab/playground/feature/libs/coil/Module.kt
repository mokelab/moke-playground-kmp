package com.mokelab.playground.feature.libs.coil

import com.mokelab.playground.core.devportal.singleScreenFeature
import org.koin.dsl.module

val featureModule = module {
    singleScreenFeature("Coil") { back -> CoilScreen(back = back) }
    singleScreenFeature("Coil placeholder / error") { back -> CoilStateScreen(back = back) }
}
