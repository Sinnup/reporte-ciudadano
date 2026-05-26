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
                onSuccess = { reports ->
                    val syncStates = loadSyncStates()
                    _state.update { it.copy(reports = reports, isLoading = false, syncStates = syncStates) }
                },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private suspend fun loadSyncStates(): Map<String, SyncStatus> =
        syncStateRepository?.getSyncStates()?.getOrElse { emptyMap() } ?: emptyMap()
}
