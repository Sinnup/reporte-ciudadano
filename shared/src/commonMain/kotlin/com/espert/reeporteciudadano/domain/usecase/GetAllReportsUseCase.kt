package com.espert.reeporteciudadano.domain.usecase

import com.espert.reeporteciudadano.domain.model.CitizenReport
import com.espert.reeporteciudadano.domain.repository.ReportRepository

class GetAllReportsUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(): Result<List<CitizenReport>> = repository.getAll()
}
