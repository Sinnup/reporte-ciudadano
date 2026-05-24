package com.espert.reporteciudadano.platform

import androidx.compose.runtime.*

@Composable
actual fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    LaunchedEffect(Unit) { onGranted() }
}
