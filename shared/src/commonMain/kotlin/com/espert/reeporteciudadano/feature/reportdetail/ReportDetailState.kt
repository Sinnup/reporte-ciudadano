package com.espert.reeporteciudadano.feature.reportdetail

import com.espert.reeporteciudadano.domain.model.CitizenReport

data class ReportDetailState(
    val report: CitizenReport? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
