package com.espert.reeporteciudadano.feature.reportform

data class ReportFormState(
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val isLoadingAddress: Boolean = true,
    val isSubmitting: Boolean = false,
    val showDisclaimer: Boolean = true,
    val submitted: Boolean = false
) {
    val canSubmit: Boolean get() = title.isNotBlank() && description.isNotBlank() && !isSubmitting
}
