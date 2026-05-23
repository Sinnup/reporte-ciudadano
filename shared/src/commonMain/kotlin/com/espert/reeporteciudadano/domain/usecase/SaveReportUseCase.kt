package com.espert.reeporteciudadano.domain.usecase

import com.espert.reeporteciudadano.domain.model.CitizenReport
import com.espert.reeporteciudadano.domain.repository.ReportRepository

class SaveReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(report: CitizenReport): Result<Unit> = repository.save(report)
}
