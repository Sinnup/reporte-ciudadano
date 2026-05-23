package com.espert.reeporteciudadano.feature.camera

import com.espert.reeporteciudadano.navigation.CapturedPhoto

data class CameraState(
    val photos: List<CapturedPhoto> = emptyList(),
    val showOptions: Boolean = false,
    val locationDenied: Boolean = false
) {
    val isFull: Boolean get() = photos.size >= 4
}
