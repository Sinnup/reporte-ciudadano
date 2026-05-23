package com.espert.reeporteciudadano.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponse(@SerialName("display_name") val displayName: String)

class GeocodingApi(private val client: HttpClient) {
    suspend fun reverseGeocode(lat: Double, lon: Double): String =
        client.get("https://nominatim.openstreetmap.org/reverse") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("format", "json")
            header("User-Agent", "ReporteCiudadano/1.0")
        }.body<NominatimResponse>().displayName
}
