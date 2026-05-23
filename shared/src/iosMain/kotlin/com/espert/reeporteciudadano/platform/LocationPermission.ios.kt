package com.espert.reeporteciudadano.platform

import androidx.compose.runtime.*

@Composable
actual fun RequestLocationPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    LaunchedEffect(Unit) { onGranted() }
}
