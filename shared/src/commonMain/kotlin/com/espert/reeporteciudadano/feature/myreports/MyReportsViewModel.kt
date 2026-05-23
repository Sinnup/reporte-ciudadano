package com.espert.reeporteciudadano.feature.myreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reeporteciudadano.domain.usecase.GetAllReportsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyReportsViewModel(private val getAllReports: GetAllReportsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(MyReportsState())
    val state: StateFlow<MyReportsState> = _state.asStateFlow()

    init { load() }

    fun processIntent(intent: MyReportsIntent) {
        when (intent) {
            is MyReportsIntent.SelectReport -> { /* handled by callback */ }
            MyReportsIntent.Refresh -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllReports().fold(
                onSuccess = { reports -> _state.update { it.copy(reports = reports, isLoading = false) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
