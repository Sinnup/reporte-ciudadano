package com.espert.reporteciudadano.feature.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    fun processIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.Reset -> _state.value = CameraState()
            is CameraIntent.PhotoTaken -> _state.update { it.copy(photos = it.photos + intent.photo, showOptions = true) }
            is CameraIntent.DeletePhoto -> {
                _state.update { current ->
                    val remaining = current.photos.filter { p -> p.id != intent.id }
                    val hasLocation = remaining.any { it.exifLocation != null }
                    current.copy(
                        photos = remaining,
                        noLocationOnPhotos = if (hasLocation) false else current.noLocationOnPhotos
                    )
                }
            }
            CameraIntent.KeepTaking -> _state.update { it.copy(showOptions = false) }
            CameraIntent.Complete -> _state.update { it.copy(showOptions = false) }
            CameraIntent.LocationDenied -> _state.update { it.copy(locationDenied = true) }
            CameraIntent.CameraDenied -> _state.update { it.copy(cameraDenied = true) }
            CameraIntent.LocationServiceDisabled -> _state.update { it.copy(locationDisabled = true) }
            CameraIntent.LocationServiceEnabled -> _state.update { it.copy(locationDisabled = false) }
            CameraIntent.NoLocationOnPhotos -> _state.update { it.copy(noLocationOnPhotos = true) }
            CameraIntent.LocationOnPhotosClear -> _state.update { it.copy(noLocationOnPhotos = false) }
        }
    }
}
