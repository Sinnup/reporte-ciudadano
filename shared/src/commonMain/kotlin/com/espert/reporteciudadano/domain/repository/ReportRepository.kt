package com.espert.reporteciudadano.domain.repository

import com.espert.reporteciudadano.domain.model.CitizenReport

interface ReportRepository {
    suspend fun save(report: CitizenReport): Result<Unit>
    suspend fun getAll(): Result<List<CitizenReport>>
    suspend fun getById(id: String): Result<CitizenReport>
}
