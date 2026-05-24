package com.espert.reporteciudadano.platform

import androidx.compose.runtime.Composable
import com.espert.reporteciudadano.navigation.CapturedPhoto

@Composable
expect fun CameraCapture(
    onPhotoTaken: (CapturedPhoto) -> Unit,
    onCancel: () -> Unit
)
