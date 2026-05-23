package com.espert.reeporteciudadano.platform

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.espert.reeporteciudadano.domain.model.GeoLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView as OsmMapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun MapView(
    reports: List<Pair<GeoLocation, String>>,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            Configuration.getInstance().apply {
                osmdroidBasePath = ctx.filesDir
                osmdroidTileCache = java.io.File(ctx.cacheDir, "osmdroid")
                userAgentValue = "ReporteCiudadano"
            }
            OsmMapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            reports.forEach { (location, reportId) ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(location.latitude, location.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ -> onMarkerClick(reportId); true }
                }
                mapView.overlays.add(marker)
            }
            if (reports.isNotEmpty()) {
                val first = reports.first().first
                mapView.controller.setCenter(GeoPoint(first.latitude, first.longitude))
            }
            mapView.invalidate()
        }
    )
}
