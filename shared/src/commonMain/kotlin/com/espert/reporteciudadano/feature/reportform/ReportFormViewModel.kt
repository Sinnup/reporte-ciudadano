package com.espert.reporteciudadano.feature.reportform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espert.reporteciudadano.domain.model.*
import com.espert.reporteciudadano.domain.usecase.ReverseGeocodeUseCase
import com.espert.reporteciudadano.domain.usecase.SaveReportUseCase
import com.espert.reporteciudadano.navigation.CapturedPhoto
import com.espert.reporteciudadano.platform.generateUuid
import com.espert.reporteciudadano.platform.isNetworkAvailable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ReportFormViewModel(
    private val saveReportUseCase: SaveReportUseCase,
    private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
    private val networkAvailable: () -> Boolean = { isNetworkAvailable() },
    private val onReportSaved: () -> Unit = {}
) : ViewModel() {
    private val _state = MutableStateFlow(ReportFormState())
    val state: StateFlow<ReportFormState> = _state.asStateFlow()

    private val _submitted = Channel<Unit>(Channel.BUFFERED)
    val submitted = _submitted.receiveAsFlow()

    private var photos: List<CapturedPhoto> = emptyList()
    private var resolvedLocation: GeoLocation? = null

    fun init(capturedPhotos: List<CapturedPhoto>) {
        _state.value = ReportFormState()
        photos = capturedPhotos
        val location = capturedPhotos.firstOrNull { it.exifLocation != null }?.exifLocation
            ?: return
        resolvedLocation = location
        viewModelScope.launch {
            if (networkAvailable()) {
                val result = reverseGeocodeUseCase(location)
                val display = result.fold(
                    onSuccess = { address -> LocationDisplay.Address(address) },
                    onFailure = { LocationDisplay.Coordinates(location.latitude, location.longitude) }
                )
                _state.update { it.copy(locationDisplay = display) }
            } else {
                _state.update {
                    it.copy(
                        locationDisplay = LocationDisplay.Coordinates(
                            location.latitude,
                            location.longitude
                        )
                    )
                }
            }
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
        val location = resolvedLocation ?: return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val report = CitizenReport(
                id = generateUuid(),
                title = s.title,
                description = s.description,
                photos = photos.map { ReportPhoto(it.id, it.localPath, it.exifLocation) },
                location = location,
                status = ReportStatus.SENT,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            saveReportUseCase(report)
            onReportSaved()
            _state.update { it.copy(isSubmitting = false) }
            _submitted.send(Unit)
        }
    }
}
