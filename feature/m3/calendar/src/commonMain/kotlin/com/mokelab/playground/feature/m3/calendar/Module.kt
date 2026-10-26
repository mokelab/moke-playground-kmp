package com.mokelab.playground.feature.m3.calendar

import com.mokelab.playground.core.devportal.singleScreenFeature
import org.koin.dsl.module

val featureModule = module {
    singleScreenFeature("M3 Calendar") { back -> CalendarScreen(back = back) }
}
