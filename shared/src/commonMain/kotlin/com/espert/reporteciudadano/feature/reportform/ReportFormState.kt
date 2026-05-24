package com.espert.reporteciudadano.feature.reportform

import com.espert.reporteciudadano.domain.model.LocationDisplay

data class ReportFormState(
    val title: String = "",
    val description: String = "",
    val locationDisplay: LocationDisplay = LocationDisplay.Loading,
    val isSubmitting: Boolean = false,
    val showDisclaimer: Boolean = true
) {
    val canSubmit: Boolean get() = title.isNotBlank() && description.isNotBlank() && !isSubmitting
}
