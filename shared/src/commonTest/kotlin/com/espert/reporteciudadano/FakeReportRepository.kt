package com.espert.reporteciudadano

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository

class FakeReportRepository : ReportRepository {
    val saved = mutableListOf<CitizenReport>()
    override suspend fun save(report: CitizenReport): Result<Unit> = runCatching { saved.add(report); Unit }
    override suspend fun getAll(): Result<List<CitizenReport>> = runCatching { saved.toList() }
    override suspend fun getById(id: String): Result<CitizenReport> = runCatching { saved.first { it.id == id } }
}
