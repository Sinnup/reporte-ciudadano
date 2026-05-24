package com.espert.reporteciudadano.feature.camera

import com.espert.reporteciudadano.navigation.CapturedPhoto

data class CameraState(
    val photos: List<CapturedPhoto> = emptyList(),
    val showOptions: Boolean = false,
    val locationDenied: Boolean = false,
    val cameraDenied: Boolean = false,
    val locationDisabled: Boolean = false,
    val noLocationOnPhotos: Boolean = false
) {
    val isFull: Boolean get() = photos.size >= 4
}
