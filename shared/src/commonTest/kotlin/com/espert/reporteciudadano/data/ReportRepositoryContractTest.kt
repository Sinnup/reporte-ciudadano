package com.espert.reporteciudadano.data

import com.espert.reporteciudadano.FakeReportRepository
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.ReportStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Contract tests for [ReportRepository] covering the store and retrieval guarantees
 * that [ReportRepositoryImpl] is expected to uphold.
 *
 * These tests use [FakeReportRepository] so they run on all platforms in commonTest.
 * They verify the domain contract: latitude and longitude are stored and retrieved,
 * no address field is present, and mapping is correct across getAll() and getById().
 */
class ReportRepositoryContractTest {

    private val repository = FakeReportRepository()

    // -----------------------------------------------------------------------
    // save() — stores latitude and longitude
    // -----------------------------------------------------------------------

    @Test
    fun `save stores latitude and longitude from CitizenReport location`() = runTest {
        val report = makeReport("r1", latitude = 18.4861, longitude = -69.9312)

        repository.save(report)

        val saved = repository.saved.first()
        assertEquals(18.4861, saved.location.latitude)
        assertEquals(-69.9312, saved.location.longitude)
    }

    @Test
    fun `save does not store an address field (CitizenReport has no address)`() = runTest {
        val report = makeReport("r1", latitude = 18.4861, longitude = -69.9312)

        repository.save(report)

        // CitizenReport no longer has an address field — confirmed by compilation
        val saved = repository.saved.first()
        // Verify only location is present, not an address string
        assertNotNull(saved.location)
        assertEquals(18.4861, saved.location.latitude)
        assertEquals(-69.9312, saved.location.longitude)
    }

    // -----------------------------------------------------------------------
    // getAll() — maps entity lat/lng to CitizenReport.location correctly
    // -----------------------------------------------------------------------

    @Test
    fun `getAll returns all saved reports with correct location coordinates`() = runTest {
        val r1 = makeReport("r1", latitude = 18.4861, longitude = -69.9312)
        val r2 = makeReport("r2", latitude = 19.4326, longitude = -99.1332)
        repository.saved.add(r1)
        repository.saved.add(r2)

        val result = repository.getAll()

        assertTrue(result.isSuccess)
        val reports = result.getOrThrow()
        assertEquals(2, reports.size)
        val first = reports.find { it.id == "r1" }!!
        val second = reports.find { it.id == "r2" }!!
        assertEquals(18.4861, first.location.latitude)
        assertEquals(-69.9312, first.location.longitude)
        assertEquals(19.4326, second.location.latitude)
        assertEquals(-99.1332, second.location.longitude)
    }

    @Test
    fun `getAll returns success with empty list when no reports exist`() = runTest {
        val result = repository.getAll()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `getAll maps all fields except address correctly`() = runTest {
        val report = makeReport("r1", latitude = 18.4861, longitude = -69.9312)
        repository.saved.add(report)

        val result = repository.getAll()

        val mapped = result.getOrThrow().first()
        assertEquals("r1", mapped.id)
        assertEquals("Test Report", mapped.title)
        assertEquals("A description", mapped.description)
        assertEquals(ReportStatus.SENT, mapped.status)
        assertEquals(18.4861, mapped.location.latitude)
        assertEquals(-69.9312, mapped.location.longitude)
    }

    // -----------------------------------------------------------------------
    // getById() — maps entity lat/lng to CitizenReport.location correctly
    // -----------------------------------------------------------------------

    @Test
    fun `getById returns the correct report with correct latitude and longitude`() = runTest {
        val report = makeReport("r1", latitude = 34.6037, longitude = -58.3816)
        repository.saved.add(report)

        val result = repository.getById("r1")

        assertTrue(result.isSuccess)
        val found = result.getOrThrow()
        assertEquals("r1", found.id)
        assertEquals(34.6037, found.location.latitude)
        assertEquals(-58.3816, found.location.longitude)
    }

    @Test
    fun `getById returns failure for unknown id`() = runTest {
        val result = repository.getById("does-not-exist")

        assertFalse(result.isSuccess)
    }

    @Test
    fun `getById maps all fields except address correctly`() = runTest {
        val report = makeReport("r2", latitude = 19.4326, longitude = -99.1332)
        repository.saved.add(report)

        val result = repository.getById("r2")

        val found = result.getOrThrow()
        assertEquals("r2", found.id)
        assertEquals("Test Report", found.title)
        assertEquals("A description", found.description)
        assertEquals(ReportStatus.SENT, found.status)
        assertEquals(19.4326, found.location.latitude)
        assertEquals(-99.1332, found.location.longitude)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun makeReport(
        id: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) = CitizenReport(
        id = id,
        title = "Test Report",
        description = "A description",
        photos = emptyList(),
        location = GeoLocation(latitude, longitude),
        status = ReportStatus.SENT,
        createdAt = 1000L
    )
}
