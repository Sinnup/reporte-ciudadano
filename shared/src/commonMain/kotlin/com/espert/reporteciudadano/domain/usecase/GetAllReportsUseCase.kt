package com.espert.reporteciudadano.domain.usecase

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository

class GetAllReportsUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(): Result<List<CitizenReport>> = repository.getAll()
}
