package com.espert.reeporteciudadano.platform

import androidx.compose.runtime.Composable
import com.espert.reeporteciudadano.navigation.CapturedPhoto

@Composable
expect fun CameraCapture(
    onPhotoTaken: (CapturedPhoto) -> Unit,
    onCancel: () -> Unit
)
