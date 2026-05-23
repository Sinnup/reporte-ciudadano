package com.espert.reeporteciudadano.domain.repository

import com.espert.reeporteciudadano.domain.model.GeoLocation

interface GeocodingRepository {
    suspend fun reverseGeocode(location: GeoLocation): Result<String>
}
