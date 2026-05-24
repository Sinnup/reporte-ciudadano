package com.espert.reporteciudadano.usecase

import com.espert.reporteciudadano.FakeReportRepository
import com.espert.reporteciudadano.domain.model.*
import com.espert.reporteciudadano.domain.usecase.SaveReportUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveReportUseCaseTest {
    private val repository = FakeReportRepository()
    private val useCase = SaveReportUseCase(repository)

    @Test
    fun `saving a report stores it in the repository`() = runTest {
        val report = makeReport()
        val result = useCase(report)
        assertTrue(result.isSuccess)
        assertEquals(1, repository.saved.size)
        assertEquals(report.id, repository.saved.first().id)
    }

    @Test
    fun `saved report preserves all fields`() = runTest {
        val report = makeReport()
        useCase(report)
        val saved = repository.saved.first()
        assertEquals(report.title, saved.title)
        assertEquals(report.status, saved.status)
    }

    private fun makeReport() = CitizenReport(
        id = "test-id",
        title = "Big pothole",
        description = "Very deep pothole on Main St",
        photos = emptyList(),
        location = GeoLocation(19.4, -99.1),
        status = ReportStatus.SENT,
        createdAt = 1000L
    )
}
