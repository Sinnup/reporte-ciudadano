package com.espert.reporteciudadano.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espert.reporteciudadano.domain.model.GeoLocation

@Composable
actual fun MapView(reports: List<Pair<GeoLocation, String>>, onMarkerClick: (String) -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Map is available on Android.", style = MaterialTheme.typography.bodyLarge)
    }
}
