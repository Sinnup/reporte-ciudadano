package com.espert.reporteciudadano.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.espert.reporteciudadano.domain.model.GeoLocation

@Composable
expect fun MapView(
    reports: List<Pair<GeoLocation, String>>,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
