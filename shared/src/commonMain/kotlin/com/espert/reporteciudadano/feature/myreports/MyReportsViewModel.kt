package com.espert.reporteciudadano.feature.myreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reporteciudadano.domain.model.SyncStatus
import com.espert.reporteciudadano.domain.repository.SyncStateRepository
import com.espert.reporteciudadano.domain.usecase.GetAllReportsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyReportsViewModel(
    private val getAllReports: GetAllReportsUseCase,
    private val syncStateRepository: SyncStateRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(MyReportsState())
    val state: StateFlow<MyReportsState> = _state.asStateFlow()

    init { observeReports() }

    fun processIntent(intent: MyReportsIntent) {
        when (intent) {
            is MyReportsIntent.SelectReport -> _state.update { it.copy(selectedReportId = intent.id) }
            MyReportsIntent.ClearSelection -> _state.update { it.copy(selectedReportId = null) }
            MyReportsIntent.Refresh -> refreshSyncStates()
        }
    }

    private fun refreshSyncStates() {
        viewModelScope.launch {
            _state.update { it.copy(syncStates = loadSyncStates()) }
        }
    }

    private fun observeReports() {
        viewModelScope.launch {
            getAllReports.observe()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { reports ->
                    val syncStates = loadSyncStates()
                    _state.update { it.copy(reports = reports, isLoading = false, syncStates = syncStates) }
                }
        }
    }

    private suspend fun loadSyncStates(): Map<String, SyncStatus> =
        syncStateRepository?.getSyncStates()?.getOrElse { emptyMap() } ?: emptyMap()
}
