package com.espert.reporteciudadano.feature.reportsmap

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
class ReportsMapAdaptiveTest {

    private val testDispatcher = StandardTestDispatcher()
    private val reportRepository = FakeReportRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        reportRepository.saved.add(makeReport("pin-1"))
        reportRepository.saved.add(makeReport("pin-2"))
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
    fun `SelectPin intent sets selectedReportId to the given report id`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.processIntent(ReportsMapIntent.SelectPin("pin-1"))
        advanceUntilIdle()

        assertEquals("pin-1", vm.state.value.selectedReportId)
    }

    @Test
    fun `selecting a different pin updates selectedReportId`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.processIntent(ReportsMapIntent.SelectPin("pin-1"))
        advanceUntilIdle()
        assertEquals("pin-1", vm.state.value.selectedReportId)

        vm.processIntent(ReportsMapIntent.SelectPin("pin-2"))
        advanceUntilIdle()

        assertEquals("pin-2", vm.state.value.selectedReportId)
    }

    @Test
    fun `ClearSelection intent resets selectedReportId to null`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.processIntent(ReportsMapIntent.SelectPin("pin-1"))
        advanceUntilIdle()

        vm.processIntent(ReportsMapIntent.ClearSelection)
        advanceUntilIdle()

        assertNull(vm.state.value.selectedReportId)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildViewModel() = ReportsMapViewModel(
        getAllReports = GetAllReportsUseCase(reportRepository)
    )

    private fun makeReport(id: String) = CitizenReport(
        id = id,
        title = "Report $id",
        description = "Description for $id",
        photos = emptyList(),
        location = GeoLocation(19.4326, -99.1332),
        status = ReportStatus.SENT,
        createdAt = 1000L
    )
}
