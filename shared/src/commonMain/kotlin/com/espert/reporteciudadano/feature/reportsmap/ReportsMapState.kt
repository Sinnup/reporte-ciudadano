package com.espert.reporteciudadano.feature.reportsmap

import com.espert.reporteciudadano.domain.model.GeoLocation

data class ReportsMapState(
    val pins: List<Pair<GeoLocation, String>> = emptyList(),
    val isLoading: Boolean = true
)
