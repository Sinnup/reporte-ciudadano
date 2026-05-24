package com.espert.reporteciudadano.domain.model

sealed interface LocationDisplay {
    data class Address(val text: String) : LocationDisplay
    data class Coordinates(val latitude: Double, val longitude: Double) : LocationDisplay
    data object Loading : LocationDisplay
}
