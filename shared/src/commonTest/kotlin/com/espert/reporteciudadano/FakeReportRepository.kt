package com.espert.reporteciudadano

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeReportRepository : ReportRepository {
    private val _saved = MutableStateFlow<List<CitizenReport>>(emptyList())
    val saved: FakeList = FakeList(_saved)

    class FakeList(private val flow: MutableStateFlow<List<CitizenReport>>) {
        fun add(report: CitizenReport) { flow.value = flow.value + report }
        fun first(): CitizenReport = flow.value.first()
        operator fun get(index: Int): CitizenReport = flow.value[index]
        val size: Int get() = flow.value.size
    }

    override suspend fun save(report: CitizenReport): Result<Unit> = runCatching {
        _saved.value += report
    }
    override suspend fun getAll(): Result<List<CitizenReport>> = runCatching { _saved.value.toList() }
    override suspend fun getById(id: String): Result<CitizenReport> = runCatching { _saved.value.first { it.id == id } }
    override suspend fun getByIds(ids: List<String>): Result<List<CitizenReport>> =
        runCatching { _saved.value.filter { it.id in ids } }
    override fun observeAll(): Flow<List<CitizenReport>> = _saved
    override fun observeById(id: String): Flow<CitizenReport?> =
        _saved.map { list -> list.firstOrNull { it.id == id } }
}
