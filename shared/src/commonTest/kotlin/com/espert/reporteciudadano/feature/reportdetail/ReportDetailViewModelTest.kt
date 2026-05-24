package com.espert.reporteciudadano.feature.reportdetail

import com.espert.reporteciudadano.FakeReportRepository
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.LocationDisplay
import com.espert.reporteciudadano.domain.model.ReportStatus
import com.espert.reporteciudadano.domain.repository.GeocodingRepository
import com.espert.reporteciudadano.domain.usecase.GetReportByIdUseCase
import com.espert.reporteciudadano.domain.usecase.ReverseGeocodeUseCase
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------------
    // Initial state
    // -----------------------------------------------------------------------

    @Test
    fun `locationDisplay is Loading in the initial state before coroutines run`() {
        val vm = buildViewModel(
            reportId = "r1",
            report = makeReport("r1"),
            networkAvailable = true,
            geocodeResult = Result.success("Some Address")
        )

        assertIs<LocationDisplay.Loading>(vm.state.value.locationDisplay)
    }

    // -----------------------------------------------------------------------
    // Online + geocoder returns address → Address
    // -----------------------------------------------------------------------

    @Test
    fun `when network is available and geocoder succeeds, locationDisplay becomes Address`() = runTest {
        val expectedAddress = "Av. Independencia 45, Centro, Guanajuato"
        val vm = buildViewModel(
            reportId = "r1",
            report = makeReport("r1", latitude = 18.4861, longitude = -69.9312),
            networkAvailable = true,
            geocodeResult = Result.success(expectedAddress)
        )

        advanceUntilIdle()

        val display = vm.state.value.locationDisplay
        assertIs<LocationDisplay.Address>(display)
        assertEquals(expectedAddress, display.text)
    }

    // -----------------------------------------------------------------------
    // Offline → Coordinates
    // -----------------------------------------------------------------------

    @Test
    fun `when network is unavailable, locationDisplay becomes Coordinates`() = runTest {
        val vm = buildViewModel(
            reportId = "r1",
            report = makeReport("r1", latitude = 18.4861, longitude = -69.9312),
            networkAvailable = false,
            geocodeResult = Result.success("Should not be reached")
        )

        advanceUntilIdle()

        val display = vm.state.value.locationDisplay
        assertIs<LocationDisplay.Coordinates>(display)
        assertEquals(18.4861, display.latitude)
        assertEquals(-69.9312, display.longitude)
    }

    // -----------------------------------------------------------------------
    // Online + geocoder fails → Coordinates fallback
    // -----------------------------------------------------------------------

    @Test
    fun `when network is available but geocoder fails, locationDisplay becomes Coordinates`() = runTest {
        val vm = buildViewModel(
            reportId = "r1",
            report = makeReport("r1", latitude = 19.4326, longitude = -99.1332),
            networkAvailable = true,
            geocodeResult = Result.failure(RuntimeException("Network error"))
        )

        advanceUntilIdle()

        val display = vm.state.value.locationDisplay
        assertIs<LocationDisplay.Coordinates>(display)
        assertEquals(19.4326, display.latitude)
        assertEquals(-99.1332, display.longitude)
    }

    // -----------------------------------------------------------------------
    // Report is loaded from repository correctly
    // -----------------------------------------------------------------------

    @Test
    fun `report is loaded and stored in state after coroutines run`() = runTest {
        val report = makeReport("r1")
        val vm = buildViewModel(
            reportId = "r1",
            report = report,
            networkAvailable = true,
            geocodeResult = Result.success("Any Address")
        )

        advanceUntilIdle()

        assertNotNull(vm.state.value.report)
        assertEquals("r1", vm.state.value.report?.id)
        assertEquals(false, vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    // -----------------------------------------------------------------------
    // Unknown report ID → error state
    // -----------------------------------------------------------------------

    @Test
    fun `when report id is not found, error state is set`() = runTest {
        val vm = buildViewModel(
            reportId = "unknown",
            report = null,
            networkAvailable = true,
            geocodeResult = Result.success("Any Address")
        )

        advanceUntilIdle()

        assertNull(vm.state.value.report)
        assertEquals(false, vm.state.value.isLoading)
        assertNotNull(vm.state.value.error)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildViewModel(
        reportId: String,
        report: CitizenReport?,
        networkAvailable: Boolean,
        geocodeResult: Result<String>
    ): ReportDetailViewModel {
        val repo = FakeReportRepository()
        if (report != null) repo.saved.add(report)
        val getReportByIdUseCase = GetReportByIdUseCase(repo)
        val fakeGeocodingRepo = FakeGeocodingRepository(geocodeResult)
        val reverseGeocodeUseCase = ReverseGeocodeUseCase(fakeGeocodingRepo)
        return ReportDetailViewModel(
            reportId = reportId,
            getReportById = getReportByIdUseCase,
            reverseGeocodeUseCase = reverseGeocodeUseCase,
            networkAvailable = { networkAvailable }
        )
    }

    private fun makeReport(
        id: String,
        latitude: Double = 18.4861,
        longitude: Double = -69.9312
    ) = CitizenReport(
        id = id,
        title = "Test Report",
        description = "A description",
        photos = emptyList(),
        location = GeoLocation(latitude, longitude),
        status = ReportStatus.SENT,
        createdAt = 0L
    )
}

private class FakeGeocodingRepository(
    private val result: Result<String>
) : GeocodingRepository {
    override suspend fun reverseGeocode(location: GeoLocation): Result<String> = result
}
