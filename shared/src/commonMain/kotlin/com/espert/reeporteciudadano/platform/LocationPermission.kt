package com.espert.reeporteciudadano.platform

import androidx.compose.runtime.Composable

@Composable
expect fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
)
