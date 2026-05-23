package com.espert.reeporteciudadano.feature.reportform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reeporteciudadano.domain.model.*
import com.espert.reeporteciudadano.domain.usecase.ReverseGeocodeUseCase
import com.espert.reeporteciudadano.domain.usecase.SaveReportUseCase
import com.espert.reeporteciudadano.navigation.CapturedPhoto
import com.espert.reeporteciudadano.platform.generateUuid
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ReportFormViewModel(
    private val saveReportUseCase: SaveReportUseCase,
    private val reverseGeocodeUseCase: ReverseGeocodeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ReportFormState())
    val state: StateFlow<ReportFormState> = _state.asStateFlow()

    private var photos: List<CapturedPhoto> = emptyList()

    fun init(capturedPhotos: List<CapturedPhoto>) {
        photos = capturedPhotos
        val location = capturedPhotos.firstOrNull()?.exifLocation
        if (location != null) {
            viewModelScope.launch {
                val result = reverseGeocodeUseCase(location)
                _state.update {
                    it.copy(
                        address = result.getOrElse { "Location unavailable" },
                        isLoadingAddress = false
                    )
                }
            }
        } else {
            _state.update { it.copy(address = "Location unavailable", isLoadingAddress = false) }
        }
    }

    fun processIntent(intent: ReportFormIntent) {
        when (intent) {
            is ReportFormIntent.TitleChanged -> _state.update { it.copy(title = intent.value.take(100)) }
            is ReportFormIntent.DescriptionChanged -> _state.update { it.copy(description = intent.value.take(500)) }
            ReportFormIntent.DismissDisclaimer -> _state.update { it.copy(showDisclaimer = false) }
            ReportFormIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val location = photos.firstOrNull()?.exifLocation ?: GeoLocation(0.0, 0.0)
            val report = CitizenReport(
                id = generateUuid(),
                title = s.title,
                description = s.description,
                photos = photos.map { ReportPhoto(it.id, it.localPath, it.exifLocation) },
                location = location,
                address = s.address,
                status = ReportStatus.SENT,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            saveReportUseCase(report)
            _state.update { it.copy(isSubmitting = false, submitted = true) }
        }
    }
}
