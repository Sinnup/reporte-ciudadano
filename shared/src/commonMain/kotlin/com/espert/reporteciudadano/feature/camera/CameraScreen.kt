package com.espert.reporteciudadano.feature.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.espert.reporteciudadano.navigation.CapturedPhoto
import com.espert.reporteciudadano.platform.CameraCapture
import com.espert.reporteciudadano.platform.RequestCameraPermission
import com.espert.reporteciudadano.platform.RequestLocationPermission
import com.espert.reporteciudadano.platform.isLocationEnabled
import com.espert.reporteciudadano.platform.openLocationSettings
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CameraScreen(
    onPhotosReady: (List<CapturedPhoto>) -> Unit,
    onCancel: () -> Unit,
    viewModel: CameraViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { viewModel.processIntent(CameraIntent.Reset) }
    val state by viewModel.state.collectAsState()
    var locationChecked by remember { mutableStateOf(false) }
    var cameraChecked by remember { mutableStateOf(false) }

    if (state.locationDenied) {
        LocationDeniedContent(onCancel = onCancel)
        return
    }

    if (state.cameraDenied) {
        CameraDeniedContent(onCancel = onCancel)
        return
    }

    if (state.locationDisabled) {
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            if (isLocationEnabled()) viewModel.processIntent(CameraIntent.LocationServiceEnabled)
        }
        LocationDisabledContent(onCancel = onCancel)
        return
    }

    RequestLocationPermission(
        onGranted = {
            if (!isLocationEnabled()) {
                viewModel.processIntent(CameraIntent.LocationServiceDisabled)
            } else {
                locationChecked = true
            }
        },
        onDenied = { viewModel.processIntent(CameraIntent.LocationDenied) }
    )

    if (locationChecked) {
        RequestCameraPermission(
            onGranted = { cameraChecked = true },
            onDenied = { viewModel.processIntent(CameraIntent.CameraDenied) }
        )
    }

    if (!cameraChecked) return

    if (state.showOptions) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.photo_taken_dialog_title)) },
            text = { Text(stringResource(Res.string.photo_taken_dialog_body)) },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!state.isFull) {
                        TextButton(onClick = { viewModel.processIntent(CameraIntent.KeepTaking) }) { Text(stringResource(Res.string.keep_taking_button)) }
                    }
                    TextButton(onClick = { viewModel.processIntent(CameraIntent.DeletePhoto(state.photos.last().id)) }) { Text(stringResource(Res.string.retake_button)) }
                    Button(onClick = {
                        viewModel.processIntent(CameraIntent.Complete)
                        onPhotosReady(state.photos)
                    }) { Text(stringResource(Res.string.complete_button)) }
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
            Text(stringResource(Res.string.location_required_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(Res.string.location_required_body),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onCancel) { Text(stringResource(Res.string.cancel_button)) }
        }
    }
}

@Composable
private fun CameraDeniedContent(onCancel: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp))
            Text(stringResource(Res.string.camera_required_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(Res.string.camera_required_body),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onCancel) { Text(stringResource(Res.string.cancel_button)) }
        }
    }
}

@Composable
private fun LocationDisabledContent(onCancel: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(stringResource(Res.string.location_service_disabled_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(Res.string.location_service_disabled_body),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { openLocationSettings() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.go_to_settings_button))
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.cancel_button))
            }
        }
    }
}
