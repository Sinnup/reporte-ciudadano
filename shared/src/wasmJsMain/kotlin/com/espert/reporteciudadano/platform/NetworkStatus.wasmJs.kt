package com.espert.reporteciudadano.platform

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
actual fun isNetworkAvailable(): Boolean = navigatorOnLine()

@OptIn(ExperimentalWasmJsInterop::class)
private fun navigatorOnLine(): Boolean = js("navigator.onLine")
