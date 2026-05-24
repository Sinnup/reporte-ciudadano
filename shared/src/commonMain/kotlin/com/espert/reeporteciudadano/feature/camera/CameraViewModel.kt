package com.espert.reeporteciudadano.feature.camera

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
            is CameraIntent.PhotoTaken -> _state.update { it.copy(photos = it.photos + intent.photo, showOptions = true) }
            is CameraIntent.DeletePhoto -> _state.update { it.copy(photos = it.photos.filter { p -> p.id != intent.id }) }
            CameraIntent.KeepTaking -> _state.update { it.copy(showOptions = false) }
            CameraIntent.Complete -> _state.update { it.copy(showOptions = false) }
            CameraIntent.LocationDenied -> _state.update { it.copy(locationDenied = true) }
            CameraIntent.CameraDenied -> _state.update { it.copy(cameraDenied = true) }
        }
    }
}
