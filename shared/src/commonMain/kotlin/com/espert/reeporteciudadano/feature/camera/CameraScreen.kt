package com.espert.reeporteciudadano.feature.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reeporteciudadano.navigation.CapturedPhoto
import com.espert.reeporteciudadano.platform.CameraCapture
import com.espert.reeporteciudadano.platform.RequestLocationPermission
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CameraScreen(
    onPhotosReady: (List<CapturedPhoto>) -> Unit,
    onCancel: () -> Unit,
    viewModel: CameraViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.locationDenied) {
        LocationDeniedContent(onCancel = onCancel)
        return
    }

    RequestLocationPermission(
        onGranted = {},
        onDenied = { viewModel.processIntent(CameraIntent.LocationDenied) }
    )

    if (state.showOptions) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Photo taken") },
            text = { Text("What would you like to do?") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!state.isFull) {
                        TextButton(onClick = { viewModel.processIntent(CameraIntent.KeepTaking) }) { Text("Keep taking") }
                    }
                    TextButton(onClick = { viewModel.processIntent(CameraIntent.DeletePhoto(state.photos.last().id)) }) { Text("Retake") }
                    Button(onClick = {
                        viewModel.processIntent(CameraIntent.Complete)
                        onPhotosReady(state.photos)
                    }) { Text("Complete") }
                }
            },
            dismissButton = {}
        )
    } else if (!state.isFull) {
        CameraCapture(
            onPhotoTaken = { photo -> viewModel.processIntent(CameraIntent.PhotoTaken(photo)) },
            onCancel = onCancel
        )
    } else {
        LaunchedEffect(Unit) { onPhotosReady(state.photos) }
    }
}

@Composable
private fun LocationDeniedContent(onCancel: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp))
            Text("Location Required", style = MaterialTheme.typography.titleLarge)
            Text(
                "Location access is needed to tag photos with GPS coordinates. Please enable location in Settings.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
