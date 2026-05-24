package com.espert.reeporteciudadano.platform

import androidx.compose.runtime.Composable

@Composable
expect fun RequestCameraPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
)
