package com.espert.reporteciudadano.feature.reportsmap

sealed class ReportsMapIntent {
    data class SelectPin(val reportId: String) : ReportsMapIntent()
    data object ClearSelection : ReportsMapIntent()
}
