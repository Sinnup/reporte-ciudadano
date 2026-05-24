package com.espert.reporteciudadano.data.repository

import com.espert.reporteciudadano.data.datasource.remote.GeocodingApi
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.repository.GeocodingRepository

class GeocodingRepositoryImpl(private val api: GeocodingApi) : GeocodingRepository {
    override suspend fun reverseGeocode(location: GeoLocation): Result<String> =
        runCatching { api.reverseGeocode(location.latitude, location.longitude) }
}
