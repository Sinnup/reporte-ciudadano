package com.espert.reporteciudadano.feature.reportdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reporteciudadano.domain.usecase.GetReportByIdUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportDetailViewModel(
    private val reportId: String,
    private val getReportById: GetReportByIdUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ReportDetailState())
    val state: StateFlow<ReportDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getReportById(reportId).fold(
                onSuccess = { report -> _state.update { s -> s.copy(report = report, isLoading = false) } },
                onFailure = { e -> _state.update { s -> s.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
