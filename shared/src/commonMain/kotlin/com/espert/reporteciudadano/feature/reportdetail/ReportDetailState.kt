package com.espert.reporteciudadano.feature.reportdetail

import com.espert.reporteciudadano.domain.model.CitizenReport

data class ReportDetailState(
    val report: CitizenReport? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
