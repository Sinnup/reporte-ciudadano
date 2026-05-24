package com.espert.reporteciudadano.usecase

import com.espert.reporteciudadano.FakeReportRepository
import com.espert.reporteciudadano.domain.model.*
import com.espert.reporteciudadano.domain.usecase.GetAllReportsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllReportsUseCaseTest {
    private val repository = FakeReportRepository()
    private val useCase = GetAllReportsUseCase(repository)

    @Test
    fun `returns empty list when no reports exist`() = runTest {
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun `returns all saved reports`() = runTest {
        repository.saved.add(makeReport("1"))
        repository.saved.add(makeReport("2"))
        val result = useCase()
        assertEquals(2, result.getOrThrow().size)
    }

    private fun makeReport(id: String) = CitizenReport(
        id, "T", "D", emptyList(), GeoLocation(0.0, 0.0), ReportStatus.SENT, 0L
    )
}
