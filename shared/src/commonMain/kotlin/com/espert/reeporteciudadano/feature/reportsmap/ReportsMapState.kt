package com.espert.reeporteciudadano.feature.reportsmap

import com.espert.reeporteciudadano.domain.model.GeoLocation

data class ReportsMapState(
    val pins: List<Pair<GeoLocation, String>> = emptyList(),
    val isLoading: Boolean = true
)
