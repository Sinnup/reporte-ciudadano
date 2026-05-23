package com.espert.reeporteciudadano.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.espert.reeporteciudadano.domain.model.GeoLocation

@Composable
expect fun MapView(
    reports: List<Pair<GeoLocation, String>>,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
