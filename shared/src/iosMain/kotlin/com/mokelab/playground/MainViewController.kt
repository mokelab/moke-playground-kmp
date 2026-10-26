package com.mokelab.playground

import com.mokelab.devportal.kmp.DevPortalViewController

/** Swift から呼ぶ。引数なしの [initKoin] は Swift から見えにくいのでラップしている */
fun doInitKoin() = initKoin()

@Suppress("FunctionName")
fun MainViewController() = DevPortalViewController()
