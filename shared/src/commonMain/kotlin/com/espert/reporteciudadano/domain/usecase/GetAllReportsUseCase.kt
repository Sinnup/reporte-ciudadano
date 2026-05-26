package com.espert.reporteciudadano.domain.usecase

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow

class GetAllReportsUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(): Result<List<CitizenReport>> = repository.getAll()
    fun observe(): Flow<List<CitizenReport>> = repository.observeAll()
}
