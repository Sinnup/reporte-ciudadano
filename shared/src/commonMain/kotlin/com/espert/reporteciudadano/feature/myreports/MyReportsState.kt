package com.espert.reporteciudadano.feature.myreports

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.SyncStatus

data class MyReportsState(
    val reports: List<CitizenReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Sync status per report ID. Reports absent from the map default to PENDING. */
    val syncStates: Map<String, SyncStatus> = emptyMap(),
    /** ID of the report selected in two-pane expanded layout. Null means no report selected. */
    val selectedReportId: String? = null
)
