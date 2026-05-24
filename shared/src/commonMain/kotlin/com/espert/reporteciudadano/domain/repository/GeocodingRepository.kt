package com.espert.reporteciudadano.domain.repository

import com.espert.reporteciudadano.domain.model.GeoLocation

interface GeocodingRepository {
    suspend fun reverseGeocode(location: GeoLocation): Result<String>
}
