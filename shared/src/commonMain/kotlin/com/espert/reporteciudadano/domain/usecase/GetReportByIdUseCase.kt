package com.espert.reporteciudadano.domain.usecase

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository

class GetReportByIdUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(id: String): Result<CitizenReport> = repository.getById(id)
}
