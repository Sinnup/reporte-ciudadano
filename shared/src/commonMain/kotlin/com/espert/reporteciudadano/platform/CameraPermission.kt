package com.espert.reporteciudadano.platform

import androidx.compose.runtime.Composable

@Composable
expect fun RequestCameraPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
)
