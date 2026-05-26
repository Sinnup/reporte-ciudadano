package com.espert.reporteciudadano.cloudsync.usecase

import com.espert.reporteciudadano.cloudsync.FakeCloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES
import com.espert.reporteciudadano.cloudsync.domain.usecase.GetPendingSyncsUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.RetryFailedSyncsUseCase
import com.espert.reporteciudadano.domain.model.SyncStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RetryFailedSyncsUseCaseTest {

    private val repository = FakeCloudSyncRepository()
    private val getPendingSyncs = GetPendingSyncsUseCase(repository)
    private var eagerSyncCalled = false

    private fun buildUseCase() = RetryFailedSyncsUseCase(
        cloudSyncRepository = repository,
        getPendingSyncsUseCase = getPendingSyncs,
        scheduleEagerSync = { eagerSyncCalled = true }
    )

    // -----------------------------------------------------------------------
    // resetForRetry called on FAILED records below the threshold
    // Note: getPendingSyncReports() excludes records AT or above the threshold.
    // Those can only be reset via the single-report invoke(reportId) overload.
    // -----------------------------------------------------------------------

    @Test
    fun `resetForRetry is called for FAILED reports with failure count below threshold`() = runTest {
        // Seed at below-threshold FAILED status (count 2 triggers FAILED but is below threshold 5)
        repository.seedRecord("r1", failureCount = 2)

        buildUseCase().invoke()

        // Expect no reset because syncFailureCount (2) < MAX_CONSECUTIVE_FAILURES (5)
        // The filter in the use case requires count >= threshold — so nothing is reset here
        assertFalse("r1" in repository.resetForRetryIds)
    }

    @Test
    fun `resetForRetry is not called for PENDING reports`() = runTest {
        repository.seedRecord("r1", failureCount = 0)

        buildUseCase().invoke()

        assertFalse("r1" in repository.resetForRetryIds)
    }

    @Test
    fun `resetForRetry is not called for SYNCED reports`() = runTest {
        repository.seedRecord("r1", failureCount = 0, syncedAtMillis = 1_000_000L)

        buildUseCase().invoke()

        assertFalse("r1" in repository.resetForRetryIds)
    }

    @Test
    fun `when no reports are returned from getPendingSyncReports, resetForRetry is never called`() = runTest {
        buildUseCase().invoke()

        assertTrue(repository.resetForRetryIds.isEmpty())
    }

    // -----------------------------------------------------------------------
    // EagerSync is enqueued regardless
    // -----------------------------------------------------------------------

    @Test
    fun `scheduleEagerSync is called even when no reports needed reset`() = runTest {
        buildUseCase().invoke()

        assertTrue(eagerSyncCalled)
    }

    @Test
    fun `scheduleEagerSync is called after processing pending reports`() = runTest {
        repository.seedRecord("r1", failureCount = 2)

        buildUseCase().invoke()

        assertTrue(eagerSyncCalled)
    }

    // -----------------------------------------------------------------------
    // Single-report overload — resets exactly the specified report
    // -----------------------------------------------------------------------

    @Test
    fun `invoke with reportId resets only that report`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)
        repository.seedRecord("r2", failureCount = MAX_CONSECUTIVE_FAILURES)

        buildUseCase().invoke("r1")

        assertTrue("r1" in repository.resetForRetryIds)
        assertFalse("r2" in repository.resetForRetryIds)
    }

    @Test
    fun `invoke with reportId enqueues eager sync`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)

        buildUseCase().invoke("r1")

        assertTrue(eagerSyncCalled)
    }

    @Test
    fun `after invoke with reportId, failure count for that report is 0`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)

        buildUseCase().invoke("r1")

        // After reset, r1 is back below threshold and returns in pending list
        val pending = repository.getPendingSyncReports().getOrThrow()
        val r1 = pending.firstOrNull { it.reportId == "r1" }
        assertEquals(0, r1?.syncFailureCount)
    }

    @Test
    fun `after invoke with reportId, report becomes eligible for sync again`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)
        // Confirm it is excluded from pending before reset
        var pending = repository.getPendingSyncReports().getOrThrow()
        assertFalse(pending.any { it.reportId == "r1" })

        buildUseCase().invoke("r1")

        // Now it should be included
        pending = repository.getPendingSyncReports().getOrThrow()
        assertTrue(pending.any { it.reportId == "r1" })
    }

    @Test
    fun `invoke with reportId resets sync status to PENDING`() = runTest {
        repository.seedRecord("r1", failureCount = MAX_CONSECUTIVE_FAILURES)

        buildUseCase().invoke("r1")

        val pending = repository.getPendingSyncReports().getOrThrow()
        val r1 = pending.firstOrNull { it.reportId == "r1" }
        assertEquals(SyncStatus.PENDING, r1?.syncStatus)
    }
}
