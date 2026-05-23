package com.espert.reeporteciudadano.usecase

import com.espert.reeporteciudadano.FakeReportRepository
import com.espert.reeporteciudadano.domain.model.*
import com.espert.reeporteciudadano.domain.usecase.GetReportByIdUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetReportByIdUseCaseTest {
    private val repository = FakeReportRepository()
    private val useCase = GetReportByIdUseCase(repository)

    @Test
    fun `returns the correct report by id`() = runTest {
        val report = CitizenReport("abc", "Title", "Desc", emptyList(), GeoLocation(0.0, 0.0), "", ReportStatus.SENT, 0L)
        repository.saved.add(report)
        val result = useCase("abc")
        assertTrue(result.isSuccess)
        assertEquals("abc", result.getOrThrow().id)
    }

    @Test
    fun `returns failure for unknown id`() = runTest {
        val result = useCase("unknown")
        assertTrue(result.isFailure)
    }
}
