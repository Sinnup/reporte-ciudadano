package com.espert.reporteciudadano.cloudsync.repository

import com.espert.reporteciudadano.cloudsync.FakeCloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.ReportStatus
import com.espert.reporteciudadano.domain.model.SyncStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Contract tests for [CloudSyncRepository] that describe the expected behavior
 * verified against [FakeCloudSyncRepository], which mirrors the contract
 * [CloudSyncRepositoryImpl] must satisfy.
 */
class CloudSyncRepositoryContractTest {

    private val repository = FakeCloudSyncRepository()

    // -----------------------------------------------------------------------
    // syncReport — delegates to DynamoDb data source
    // -----------------------------------------------------------------------

    @Test
    fun `syncReport success path records the call`() = runTest {
        val report = makeReport("r1")

        val result = repository.syncReport(report)

        assertTrue(result.isSuccess)
        assertTrue("r1" in repository.syncedReportIds)
    }

    @Test
    fun `syncReport failure path returns failure`() = runTest {
        repository.syncReportResult = Result.failure(RuntimeException("DynamoDB unavailable"))
        val report = makeReport("r1")

        val result = repository.syncReport(report)

        assertFalse(result.isSuccess)
    }

    // -----------------------------------------------------------------------
    // syncPhoto — delegates to S3 data source
    // -----------------------------------------------------------------------

    @Test
    fun `syncPhoto success path records the call with correct key components`() = runTest {
        val result = repository.syncPhoto("r1", "/local/photos/image.jpg")

        assertTrue(result.isSuccess)
        assertTrue(repository.syncedPhotoPaths.any { it.first == "r1" && it.second == "/local/photos/image.jpg" })
    }

    @Test
    fun `syncPhoto failure path returns failure`() = runTest {
        repository.syncPhotoResult = Result.failure(RuntimeException("S3 unavailable"))

        val result = repository.syncPhoto("r1", "/local/photos/image.jpg")

        assertFalse(result.isSuccess)
    }

    // -----------------------------------------------------------------------
    // getUnsyncedReports / getPendingSyncReports
    // -----------------------------------------------------------------------

    @Test
    fun `getPendingSyncReports returns reports with synced_at null as PENDING`() = runTest {
        repository.seedRecord("r1", failureCount = 0, syncedAtMillis = null)

        val result = repository.getPendingSyncReports()

        assertTrue(result.isSuccess)
        val records = result.getOrThrow()
        val r1 = records.first { it.reportId == "r1" }
        assertEquals(SyncStatus.PENDING, r1.syncStatus)
        assertNull(r1.syncedAt)
    }

    @Test
    fun `getPendingSyncReports returns reports with failure count greater than 0 as FAILED`() = runTest {
        repository.seedRecord("r2", failureCount = 3)

        val records = repository.getPendingSyncReports().getOrThrow()

        val r2 = records.first { it.reportId == "r2" }
        assertEquals(SyncStatus.FAILED, r2.syncStatus)
        assertEquals(3, r2.syncFailureCount)
    }

    @Test
    fun `getPendingSyncReports excludes reports at or above the failure threshold`() = runTest {
        repository.seedRecord("r-ok", failureCount = MAX_CONSECUTIVE_FAILURES - 1)
        repository.seedRecord("r-exhausted", failureCount = MAX_CONSECUTIVE_FAILURES)

        val records = repository.getPendingSyncReports().getOrThrow()

        assertTrue(records.any { it.reportId == "r-ok" })
        assertFalse(records.any { it.reportId == "r-exhausted" })
    }

    @Test
    fun `getPendingSyncReports returns synced report with SYNCED status when syncedAt is set and failure count is 0`() = runTest {
        repository.seedRecord("r1", failureCount = 0, syncedAtMillis = 1_700_000_000_000L)

        val records = repository.getPendingSyncReports().getOrThrow()

        val r1 = records.first { it.reportId == "r1" }
        assertEquals(SyncStatus.SYNCED, r1.syncStatus)
    }

    // -----------------------------------------------------------------------
    // markSynced
    // -----------------------------------------------------------------------

    @Test
    fun `markSynced records the call`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        repository.markSynced("r1")

        assertTrue("r1" in repository.markedSyncedIds)
    }

    @Test
    fun `markSynced resets failure count to 0`() = runTest {
        repository.seedRecord("r1", failureCount = 3)

        repository.markSynced("r1")

        val records = repository.getPendingSyncReports().getOrThrow()
        val r1 = records.firstOrNull { it.reportId == "r1" }
        assertEquals(0, r1?.syncFailureCount)
    }

    // -----------------------------------------------------------------------
    // recordSyncFailure
    // -----------------------------------------------------------------------

    @Test
    fun `recordSyncFailure increments failure count by 1`() = runTest {
        repository.seedRecord("r1", failureCount = 2)

        repository.recordSyncFailure("r1")

        assertEquals(1, repository.recordedFailureIds.count { it == "r1" })
    }

    @Test
    fun `recordSyncFailure returns success`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        val result = repository.recordSyncFailure("r1")

        assertTrue(result.isSuccess)
    }

    // -----------------------------------------------------------------------
    // resetForRetry
    // -----------------------------------------------------------------------

    @Test
    fun `resetForRetry resets failure count to 0`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)

        repository.resetForRetry("r1")

        val records = repository.getPendingSyncReports().getOrThrow()
        val r1 = records.firstOrNull { it.reportId == "r1" }
        assertEquals(0, r1?.syncFailureCount)
    }

    @Test
    fun `resetForRetry makes report eligible for sync again`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)
        // Before reset: excluded from pending list
        var records = repository.getPendingSyncReports().getOrThrow()
        assertFalse(records.any { it.reportId == "r1" })

        repository.resetForRetry("r1")

        // After reset: appears in pending list again
        records = repository.getPendingSyncReports().getOrThrow()
        assertTrue(records.any { it.reportId == "r1" })
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun makeReport(id: String) = CitizenReport(
        id = id,
        title = "Pothole $id",
        description = "Description",
        photos = emptyList(),
        location = GeoLocation(19.4326, -99.1332),
        status = ReportStatus.SENT,
        createdAt = 1_000_000L
    )
}
