package com.espert.reporteciudadano

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeReportRepository : ReportRepository {
    private val _saved = MutableStateFlow<List<CitizenReport>>(emptyList())
    val saved: List<CitizenReport> get() = _saved.value

    override suspend fun save(report: CitizenReport): Result<Unit> = runCatching {
        _saved.value = _saved.value + report
    }
    override suspend fun getAll(): Result<List<CitizenReport>> = runCatching { _saved.value.toList() }
    override suspend fun getById(id: String): Result<CitizenReport> = runCatching { _saved.value.first { it.id == id } }
    override suspend fun getByIds(ids: List<String>): Result<List<CitizenReport>> =
        runCatching { _saved.value.filter { it.id in ids } }
    override fun observeAll(): Flow<List<CitizenReport>> = _saved
    override fun observeById(id: String): Flow<CitizenReport?> =
        _saved.map { list -> list.firstOrNull { it.id == id } }
}
