package com.mokelab.playground.feature.libs.coil

import coil3.ImageLoader
import com.mokelab.playground.core.devportal.singleScreenFeature
import org.koin.dsl.module

val featureModule = module {
    single<ImageLoader> { newImageLoader(platformContext()) }

    singleScreenFeature("Coil") { back -> CoilScreen(back = back) }
    singleScreenFeature("Coil placeholder / error") { back -> CoilStateScreen(back = back) }
    singleScreenFeature("Coil cache") { back -> CoilCacheScreen(back = back) }
    singleScreenFeature("Coil ImageLoader (Koin)") { back -> CoilKoinScreen(back = back) }
    singleScreenFeature("Coil transformation") { back -> CoilTransformationScreen(back = back) }
    singleScreenFeature("Coil size / scale") { back -> CoilSizeScreen(back = back) }
    singleScreenFeature("Coil SVG") { back -> CoilSvgScreen(back = back) }
}
