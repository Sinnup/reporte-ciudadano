package com.espert.reporteciudadano.feature.reportdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reporteciudadano.domain.model.LocationDisplay
import com.espert.reporteciudadano.domain.usecase.GetReportByIdUseCase
import com.espert.reporteciudadano.domain.usecase.ReverseGeocodeUseCase
import com.espert.reporteciudadano.platform.isNetworkAvailable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportDetailViewModel(
    private val reportId: String,
    private val getReportById: GetReportByIdUseCase,
    private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
    private val networkAvailable: () -> Boolean = { isNetworkAvailable() }
) : ViewModel() {
    private val _state = MutableStateFlow(ReportDetailState())
    val state: StateFlow<ReportDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getReportById(reportId).fold(
                onSuccess = { report ->
                    _state.update { s -> s.copy(report = report, isLoading = false) }
                    resolveLocation(report.location.latitude, report.location.longitude)
                },
                onFailure = { e -> _state.update { s -> s.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun resolveLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val display = if (networkAvailable()) {
                val location = com.espert.reporteciudadano.domain.model.GeoLocation(latitude, longitude)
                reverseGeocodeUseCase(location).fold(
                    onSuccess = { address -> LocationDisplay.Address(address) },
                    onFailure = { LocationDisplay.Coordinates(latitude, longitude) }
                )
            } else {
                LocationDisplay.Coordinates(latitude, longitude)
            }
            _state.update { it.copy(locationDisplay = display) }
        }
    }
}
