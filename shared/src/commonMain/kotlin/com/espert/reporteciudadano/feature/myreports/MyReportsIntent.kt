package com.espert.reporteciudadano.feature.myreports

sealed class MyReportsIntent {
    data class SelectReport(val id: String) : MyReportsIntent()
    object Refresh : MyReportsIntent()
}
