package com.espert.reporteciudadano.cloudsync.usecase

import com.espert.reporteciudadano.cloudsync.FakeCloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES
import com.espert.reporteciudadano.cloudsync.domain.usecase.RecordSyncFailureUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecordSyncFailureUseCaseTest {

    private val repository = FakeCloudSyncRepository()
    private val useCase = RecordSyncFailureUseCase(repository)

    // -----------------------------------------------------------------------
    // Increments failure count
    // -----------------------------------------------------------------------

    @Test
    fun `first failure increments count to 1 and threshold is not reached`() = runTest {
        repository.seedRecord("report-1", failureCount = 0)

        val result = useCase("report-1")

        assertTrue(result.isSuccess)
        val outcome = result.getOrThrow()
        assertEquals(1, outcome.failureCount)
        assertFalse(outcome.thresholdReached)
    }

    @Test
    fun `second failure increments count to 2 and threshold is not reached`() = runTest {
        repository.seedRecord("report-1", failureCount = 1)

        val result = useCase("report-1")

        assertTrue(result.isSuccess)
        val outcome = result.getOrThrow()
        assertEquals(2, outcome.failureCount)
        assertFalse(outcome.thresholdReached)
    }

    @Test
    fun `fourth failure increments count to 4 and threshold is not yet reached`() = runTest {
        repository.seedRecord("report-1", failureCount = 3)

        val result = useCase("report-1")

        assertTrue(result.isSuccess)
        val outcome = result.getOrThrow()
        assertEquals(4, outcome.failureCount)
        assertFalse(outcome.thresholdReached)
    }

    // -----------------------------------------------------------------------
    // Threshold reached at MAX_CONSECUTIVE_FAILURES
    // -----------------------------------------------------------------------

    @Test
    fun `when failure count reaches MAX_CONSECUTIVE_FAILURES threshold is reached`() = runTest {
        // Seed with count at threshold - 1 so this failure pushes it to threshold
        repository.seedRecord("report-1", failureCount = MAX_CONSECUTIVE_FAILURES - 1)

        val result = useCase("report-1")

        assertTrue(result.isSuccess)
        val outcome = result.getOrThrow()
        assertEquals(MAX_CONSECUTIVE_FAILURES, outcome.failureCount)
        assertTrue(outcome.thresholdReached)
    }

    @Test
    fun `MAX_CONSECUTIVE_FAILURES is defined as 5`() {
        assertEquals(5, MAX_CONSECUTIVE_FAILURES)
    }

    // -----------------------------------------------------------------------
    // Repository interaction
    // -----------------------------------------------------------------------

    @Test
    fun `recordSyncFailure is called on the repository`() = runTest {
        repository.seedRecord("report-2", failureCount = 0)

        useCase("report-2")

        assertTrue("report-2" in repository.recordedFailureIds)
    }

    @Test
    fun `multiple calls for the same report accumulate failure count`() = runTest {
        repository.seedRecord("report-3", failureCount = 0)

        useCase("report-3")
        useCase("report-3")
        val result = useCase("report-3")

        val outcome = result.getOrThrow()
        assertEquals(3, outcome.failureCount)
    }
}
