package com.espert.reporteciudadano.domain.usecase

import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.repository.GeocodingRepository

class ReverseGeocodeUseCase(private val repository: GeocodingRepository) {
    suspend operator fun invoke(location: GeoLocation): Result<String> = repository.reverseGeocode(location)
}
