package com.espert.reporteciudadano.feature.reportform

sealed class ReportFormIntent {
    data class TitleChanged(val value: String) : ReportFormIntent()
    data class DescriptionChanged(val value: String) : ReportFormIntent()
    object DismissDisclaimer : ReportFormIntent()
    object Submit : ReportFormIntent()
}
