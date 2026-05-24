package com.espert.reporteciudadano.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reporteciudadano.navigation.CapturedPhoto

@Composable
actual fun CameraCapture(onPhotoTaken: (CapturedPhoto) -> Unit, onCancel: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Camera is available on Android.", style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
