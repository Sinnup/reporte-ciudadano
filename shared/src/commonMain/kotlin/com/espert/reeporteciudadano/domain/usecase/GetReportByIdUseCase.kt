package com.espert.reeporteciudadano.domain.usecase

import com.espert.reeporteciudadano.domain.model.CitizenReport
import com.espert.reeporteciudadano.domain.repository.ReportRepository

class GetReportByIdUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(id: String): Result<CitizenReport> = repository.getById(id)
}
