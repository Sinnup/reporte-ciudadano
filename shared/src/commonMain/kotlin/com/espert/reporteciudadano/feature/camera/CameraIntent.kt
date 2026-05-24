package com.espert.reporteciudadano.feature.camera

import com.espert.reporteciudadano.navigation.CapturedPhoto

sealed class CameraIntent {
    object Reset : CameraIntent()
    data class PhotoTaken(val photo: CapturedPhoto) : CameraIntent()
    data class DeletePhoto(val id: String) : CameraIntent()
    object KeepTaking : CameraIntent()
    object Complete : CameraIntent()
    object LocationDenied : CameraIntent()
    object CameraDenied : CameraIntent()
    object LocationServiceDisabled : CameraIntent()
    object LocationServiceEnabled : CameraIntent()
    object NoLocationOnPhotos : CameraIntent()
    object LocationOnPhotosClear : CameraIntent()
}
