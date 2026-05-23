package com.espert.reeporteciudadano.feature.reportsmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reeporteciudadano.domain.usecase.GetAllReportsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportsMapViewModel(private val getAllReports: GetAllReportsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(ReportsMapState())
    val state: StateFlow<ReportsMapState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllReports().fold(
                onSuccess = { reports ->
                    _state.update { it.copy(pins = reports.map { r -> r.location to r.id }, isLoading = false) }
                },
                onFailure = { _state.update { it.copy(isLoading = false) } }
            )
        }
    }
}
