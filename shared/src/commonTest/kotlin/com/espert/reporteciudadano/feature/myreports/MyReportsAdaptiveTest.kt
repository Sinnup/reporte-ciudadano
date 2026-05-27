package com.espert.reporteciudadano.feature.myreports

import com.espert.reporteciudadano.FakeReportRepository
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.ReportStatus
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MyReportsAdaptiveTest {

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

    @Test
    fun `initial state has selectedReportId as null`() = runTest {
        val vm = buildViewModel()

        advanceUntilIdle()

        assertNull(vm.state.value.selectedReportId)
    }

    @Test
    fun `SelectReport intent sets selectedReportId to the given id`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.processIntent(MyReportsIntent.SelectReport("report-42"))
        advanceUntilIdle()

        assertEquals("report-42", vm.state.value.selectedReportId)
    }

    @Test
    fun `selecting a different report updates selectedReportId`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.processIntent(MyReportsIntent.SelectReport("report-1"))
        advanceUntilIdle()
        assertEquals("report-1", vm.state.value.selectedReportId)

        vm.processIntent(MyReportsIntent.SelectReport("report-2"))
        advanceUntilIdle()

        assertEquals("report-2", vm.state.value.selectedReportId)
    }

    @Test
    fun `ClearSelection intent resets selectedReportId to null`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.processIntent(MyReportsIntent.SelectReport("report-99"))
        advanceUntilIdle()

        vm.processIntent(MyReportsIntent.ClearSelection)
        advanceUntilIdle()

        assertNull(vm.state.value.selectedReportId)
    }

    @Test
    fun `ClearSelection on already-null selection stays null without crashing`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        assertNull(vm.state.value.selectedReportId)

        vm.processIntent(MyReportsIntent.ClearSelection)
        advanceUntilIdle()

        assertNull(vm.state.value.selectedReportId)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildViewModel() = MyReportsViewModel(
        getAllReports = GetAllReportsUseCase(reportRepository),
        syncStateRepository = null
    )

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
