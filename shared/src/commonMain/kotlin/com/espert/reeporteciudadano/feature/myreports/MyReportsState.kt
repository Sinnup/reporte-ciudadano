package com.espert.reeporteciudadano.feature.myreports

import com.espert.reeporteciudadano.domain.model.CitizenReport

data class MyReportsState(
    val reports: List<CitizenReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
