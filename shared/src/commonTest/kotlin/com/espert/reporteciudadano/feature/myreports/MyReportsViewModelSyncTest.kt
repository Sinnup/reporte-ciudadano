package com.espert.reporteciudadano.feature.myreports

import com.espert.reporteciudadano.FakeReportRepository
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.ReportStatus
import com.espert.reporteciudadano.domain.model.SyncStatus
import com.espert.reporteciudadano.domain.repository.SyncStateRepository
import com.espert.reporteciudadano.domain.usecase.GetAllReportsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MyReportsViewModelSyncTest {

    private val testDispatcher = StandardTestDispatcher()
    private val reportRepository = FakeReportRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------------
    // syncStates map is populated from the repository
    // -----------------------------------------------------------------------

    @Test
    fun `syncStates map is empty when SyncStateRepository is null`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = null
        )

        advanceUntilIdle()

        assertTrue(vm.state.value.syncStates.isEmpty())
    }

    @Test
    fun `syncStates map reflects SYNCED status returned by repository`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        val fakeSyncRepo = FakeSyncStateRepository(
            mapOf("r1" to SyncStatus.SYNCED)
        )
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = fakeSyncRepo
        )

        advanceUntilIdle()

        assertEquals(SyncStatus.SYNCED, vm.state.value.syncStates["r1"])
    }

    @Test
    fun `syncStates map reflects FAILED status when syncFailureCount reaches threshold`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        val fakeSyncRepo = FakeSyncStateRepository(
            mapOf("r1" to SyncStatus.FAILED)
        )
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = fakeSyncRepo
        )

        advanceUntilIdle()

        assertEquals(SyncStatus.FAILED, vm.state.value.syncStates["r1"])
    }

    @Test
    fun `syncStates map reflects PENDING status for a newly saved report`() = runTest {
        reportRepository.saved.add(makeReport("r2"))
        val fakeSyncRepo = FakeSyncStateRepository(
            mapOf("r2" to SyncStatus.PENDING)
        )
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = fakeSyncRepo
        )

        advanceUntilIdle()

        assertEquals(SyncStatus.PENDING, vm.state.value.syncStates["r2"])
    }

    @Test
    fun `syncStates map is populated for multiple reports with different statuses`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        reportRepository.saved.add(makeReport("r2"))
        reportRepository.saved.add(makeReport("r3"))
        val fakeSyncRepo = FakeSyncStateRepository(
            mapOf(
                "r1" to SyncStatus.SYNCED,
                "r2" to SyncStatus.FAILED,
                "r3" to SyncStatus.PENDING
            )
        )
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = fakeSyncRepo
        )

        advanceUntilIdle()

        assertEquals(SyncStatus.SYNCED, vm.state.value.syncStates["r1"])
        assertEquals(SyncStatus.FAILED, vm.state.value.syncStates["r2"])
        assertEquals(SyncStatus.PENDING, vm.state.value.syncStates["r3"])
    }

    @Test
    fun `reports absent from the sync map are not present as keys`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        reportRepository.saved.add(makeReport("r2"))
        val fakeSyncRepo = FakeSyncStateRepository(
            mapOf("r1" to SyncStatus.SYNCED)
            // r2 deliberately absent
        )
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = fakeSyncRepo
        )

        advanceUntilIdle()

        assertEquals(SyncStatus.SYNCED, vm.state.value.syncStates["r1"])
        assertEquals(null, vm.state.value.syncStates["r2"])
    }

    @Test
    fun `when SyncStateRepository returns failure, syncStates is empty`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        val failingSyncRepo = FakeSyncStateRepository(null)
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = failingSyncRepo
        )

        advanceUntilIdle()

        assertTrue(vm.state.value.syncStates.isEmpty())
    }

    @Test
    fun `Refresh intent re-loads syncStates from repository`() = runTest {
        reportRepository.saved.add(makeReport("r1"))
        val fakeSyncRepo = FakeSyncStateRepository(
            mapOf("r1" to SyncStatus.PENDING)
        )
        val vm = MyReportsViewModel(
            getAllReports = GetAllReportsUseCase(reportRepository),
            syncStateRepository = fakeSyncRepo
        )
        advanceUntilIdle()
        assertEquals(SyncStatus.PENDING, vm.state.value.syncStates["r1"])

        fakeSyncRepo.states = mapOf("r1" to SyncStatus.SYNCED)
        vm.processIntent(MyReportsIntent.Refresh)
        advanceUntilIdle()

        assertEquals(SyncStatus.SYNCED, vm.state.value.syncStates["r1"])
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun makeReport(id: String) = CitizenReport(
        id = id,
        title = "Report $id",
        description = "Description for $id",
        photos = emptyList(),
        location = GeoLocation(19.4, -99.1),
        status = ReportStatus.SENT,
        createdAt = 1000L
    )
}

private class FakeSyncStateRepository(
    initialStates: Map<String, SyncStatus>?
) : SyncStateRepository {
    var states: Map<String, SyncStatus>? = initialStates

    override suspend fun getSyncStates(): Result<Map<String, SyncStatus>> =
        states?.let { Result.success(it) }
            ?: Result.failure(RuntimeException("Sync state unavailable"))
}
