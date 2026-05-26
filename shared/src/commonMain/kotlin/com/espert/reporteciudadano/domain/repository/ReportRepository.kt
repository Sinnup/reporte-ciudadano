package com.espert.reporteciudadano.domain.repository

import com.espert.reporteciudadano.domain.model.CitizenReport
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun save(report: CitizenReport): Result<Unit>
    suspend fun getAll(): Result<List<CitizenReport>>
    suspend fun getById(id: String): Result<CitizenReport>
    /** Returns all [CitizenReport] objects for the given list of IDs. Used by sync use cases. */
    suspend fun getByIds(ids: List<String>): Result<List<CitizenReport>>
    /** Emits the full report list whenever any row in ReportEntity changes. */
    fun observeAll(): Flow<List<CitizenReport>>
    /** Emits the report whenever it changes, or null if it no longer exists. */
    fun observeById(id: String): Flow<CitizenReport?>
}
