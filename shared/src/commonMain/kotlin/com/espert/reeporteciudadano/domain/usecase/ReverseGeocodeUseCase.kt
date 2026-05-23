package com.espert.reeporteciudadano.domain.usecase

import com.espert.reeporteciudadano.domain.model.GeoLocation
import com.espert.reeporteciudadano.domain.repository.GeocodingRepository

class ReverseGeocodeUseCase(private val repository: GeocodingRepository) {
    suspend operator fun invoke(location: GeoLocation): Result<String> = repository.reverseGeocode(location)
}
