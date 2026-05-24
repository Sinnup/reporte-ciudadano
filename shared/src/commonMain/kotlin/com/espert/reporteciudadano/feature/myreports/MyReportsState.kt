package com.espert.reporteciudadano.feature.myreports

import com.espert.reporteciudadano.domain.model.CitizenReport

data class MyReportsState(
    val reports: List<CitizenReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
