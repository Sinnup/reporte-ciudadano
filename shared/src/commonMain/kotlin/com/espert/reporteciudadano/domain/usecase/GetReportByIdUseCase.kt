package com.espert.reporteciudadano.domain.usecase

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow

class GetReportByIdUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(id: String): Result<CitizenReport> = repository.getById(id)
    fun observe(id: String): Flow<CitizenReport?> = repository.observeById(id)
}
