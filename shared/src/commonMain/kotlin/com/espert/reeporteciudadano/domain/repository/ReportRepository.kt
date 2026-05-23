package com.espert.reeporteciudadano.domain.repository

import com.espert.reeporteciudadano.domain.model.CitizenReport

interface ReportRepository {
    suspend fun save(report: CitizenReport): Result<Unit>
    suspend fun getAll(): Result<List<CitizenReport>>
    suspend fun getById(id: String): Result<CitizenReport>
}
