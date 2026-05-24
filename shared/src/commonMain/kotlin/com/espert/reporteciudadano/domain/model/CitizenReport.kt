package com.espert.reporteciudadano.domain.model

data class CitizenReport(
    val id: String,
    val title: String,
    val description: String,
    val photos: List<ReportPhoto>,
    val location: GeoLocation,
    val address: String,
    val status: ReportStatus,
    val createdAt: Long
)
