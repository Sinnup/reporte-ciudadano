package com.espert.reeporteciudadano.data.repository

import com.espert.reeporteciudadano.data.datasource.remote.GeocodingApi
import com.espert.reeporteciudadano.domain.model.GeoLocation
import com.espert.reeporteciudadano.domain.repository.GeocodingRepository

class GeocodingRepositoryImpl(private val api: GeocodingApi) : GeocodingRepository {
    override suspend fun reverseGeocode(location: GeoLocation): Result<String> =
        runCatching { api.reverseGeocode(location.latitude, location.longitude) }
}
