package com.espert.reporteciudadano.cloudsync.usecase

import com.espert.reporteciudadano.cloudsync.FakeCloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.usecase.RecordSyncFailureUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportResult
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportUseCase
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.ReportPhoto
import com.espert.reporteciudadano.domain.model.ReportStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SyncReportUseCaseTest {

    private val repository = FakeCloudSyncRepository()
    private val recordFailureUseCase = RecordSyncFailureUseCase(repository)
    private val useCase = SyncReportUseCase(repository, recordFailureUseCase)

    // -----------------------------------------------------------------------
    // Success path: DynamoDB sync then markSynced
    // -----------------------------------------------------------------------

    @Test
    fun `success path calls syncReport on the repository`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        useCase(makeReport("r1"))

        assertTrue("r1" in repository.syncedReportIds)
    }

    @Test
    fun `success path calls markSynced after report sync succeeds`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        useCase(makeReport("r1"))

        assertTrue("r1" in repository.markedSyncedIds)
    }

    @Test
    fun `success path returns SyncReportResult Success`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        val result = useCase(makeReport("r1"))

        assertTrue(result.isSuccess)
        assertIs<SyncReportResult.Success>(result.getOrThrow())
    }

    @Test
    fun `success path syncs all photos via syncPhoto`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        val report = makeReport("r1", photos = listOf(
            ReportPhoto("p1", "/data/photos/a.jpg", null),
            ReportPhoto("p2", "/data/photos/b.jpg", null)
        ))

        useCase(report)

        assertTrue(repository.syncedPhotoPaths.any { it.first == "r1" && it.second == "/data/photos/a.jpg" })
        assertTrue(repository.syncedPhotoPaths.any { it.first == "r1" && it.second == "/data/photos/b.jpg" })
    }

    @Test
    fun `success path does not record any failure`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        useCase(makeReport("r1"))

        assertFalse("r1" in repository.recordedFailureIds)
    }

    // -----------------------------------------------------------------------
    // Failure path: DynamoDB sync fails → recordSyncFailure, no markSynced
    // -----------------------------------------------------------------------

    @Test
    fun `when syncReport fails, recordSyncFailure is called`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        repository.syncReportResult = Result.failure(RuntimeException("Network error"))

        useCase(makeReport("r1"))

        assertTrue("r1" in repository.recordedFailureIds)
    }

    @Test
    fun `when syncReport fails, markSynced is not called`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        repository.syncReportResult = Result.failure(RuntimeException("Network error"))

        useCase(makeReport("r1"))

        assertFalse("r1" in repository.markedSyncedIds)
    }

    @Test
    fun `when syncReport fails, result is SyncReportResult Failed`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        repository.syncReportResult = Result.failure(RuntimeException("Network error"))

        val result = useCase(makeReport("r1"))

        assertTrue(result.isSuccess)
        assertIs<SyncReportResult.Failed>(result.getOrThrow())
    }

    @Test
    fun `when syncReport failure count reaches threshold, Failed has thresholdReached true`() = runTest {
        // Seed at threshold - 1 so this failure tips it over
        repository.seedRecord("r1", failureCount = 4)
        repository.syncReportResult = Result.failure(RuntimeException("Network error"))

        val result = useCase(makeReport("r1"))

        val failed = result.getOrThrow() as SyncReportResult.Failed
        assertTrue(failed.thresholdReached)
    }

    @Test
    fun `when syncReport failure count is below threshold, Failed has thresholdReached false`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        repository.syncReportResult = Result.failure(RuntimeException("Network error"))

        val result = useCase(makeReport("r1"))

        val failed = result.getOrThrow() as SyncReportResult.Failed
        assertFalse(failed.thresholdReached)
    }

    // -----------------------------------------------------------------------
    // Photo failure path: photo sync fails → recordSyncFailure, no markSynced
    // -----------------------------------------------------------------------

    @Test
    fun `when photo sync fails, recordSyncFailure is called`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        repository.syncPhotoResult = Result.failure(RuntimeException("S3 error"))
        val report = makeReport("r1", photos = listOf(ReportPhoto("p1", "/photos/a.jpg", null)))

        useCase(report)

        assertTrue("r1" in repository.recordedFailureIds)
    }

    @Test
    fun `when photo sync fails, markSynced is not called`() = runTest {
        repository.seedRecord("r1", failureCount = 0)
        repository.syncPhotoResult = Result.failure(RuntimeException("S3 error"))
        val report = makeReport("r1", photos = listOf(ReportPhoto("p1", "/photos/a.jpg", null)))

        useCase(report)

        assertFalse("r1" in repository.markedSyncedIds)
    }

    @Test
    fun `report with no photos succeeds without any syncPhoto calls`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        val result = useCase(makeReport("r1", photos = emptyList()))

        assertTrue(result.isSuccess)
        assertIs<SyncReportResult.Success>(result.getOrThrow())
        assertTrue(repository.syncedPhotoPaths.isEmpty())
        assertTrue("r1" in repository.markedSyncedIds)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun makeReport(id: String, photos: List<ReportPhoto> = emptyList()) = CitizenReport(
        id = id,
        title = "Pothole on Main St",
        description = "Large pothole near intersection",
        photos = photos,
        location = GeoLocation(19.4326, -99.1332),
        status = ReportStatus.SENT,
        createdAt = 1_700_000_000_000L
    )
}
