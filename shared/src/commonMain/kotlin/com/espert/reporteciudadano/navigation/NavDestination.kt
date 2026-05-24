package com.espert.reporteciudadano.navigation

import com.espert.reporteciudadano.domain.model.GeoLocation

sealed class NavDestination {
    data object MainShell : NavDestination()
    data object Camera : NavDestination()
    data class PhotoReview(val photos: List<CapturedPhoto>) : NavDestination()
    data class ReportForm(val photos: List<CapturedPhoto>) : NavDestination()
    data object ThankYou : NavDestination()
    data class ReportDetail(val reportId: String) : NavDestination()
}

data class CapturedPhoto(
    val id: String,
    val localPath: String,
    val exifLocation: GeoLocation?
)

enum class BottomTab { REPORT, MY_REPORTS, MAP }
