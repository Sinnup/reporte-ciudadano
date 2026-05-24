package com.espert.reporteciudadano.domain.usecase

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository

class SaveReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(report: CitizenReport): Result<Unit> = repository.save(report)
}
