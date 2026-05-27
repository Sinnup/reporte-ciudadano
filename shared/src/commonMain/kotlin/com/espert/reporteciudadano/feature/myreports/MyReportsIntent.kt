package com.espert.reporteciudadano.feature.myreports

sealed class MyReportsIntent {
    data class SelectReport(val id: String) : MyReportsIntent()
    data object ClearSelection : MyReportsIntent()
    data object Refresh : MyReportsIntent()
}
