package com.espert.reeporteciudadano.platform

import androidx.compose.runtime.*

@Composable
actual fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    LaunchedEffect(Unit) { onGranted() }
}
