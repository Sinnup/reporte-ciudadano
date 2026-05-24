package com.espert.reporteciudadano.platform

actual fun isNetworkAvailable(): Boolean = kotlinx.browser.window.navigator.onLine
