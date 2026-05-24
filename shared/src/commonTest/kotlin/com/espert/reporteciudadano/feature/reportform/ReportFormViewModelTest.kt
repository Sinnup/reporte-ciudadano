package com.espert.reporteciudadano.feature.reportform

import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.LocationDisplay
import com.espert.reporteciudadano.domain.repository.GeocodingRepository
import com.espert.reporteciudadano.domain.usecase.ReverseGeocodeUseCase
import com.espert.reporteciudadano.domain.usecase.SaveReportUseCase
import com.espert.reporteciudadano.navigation.CapturedPhoto
import com.espert.reporteciudadano.FakeReportRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReportFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val reportRepository = FakeReportRepository()
    private val saveReportUseCase = SaveReportUseCase(reportRepository)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------------
    // locationDisplay starts as Loading
    // -----------------------------------------------------------------------

    @Test
    fun `locationDisplay is Loading before init is called`() {
        val vm = buildViewModel(networkAvailable = true, geocodeResult = Result.success("Any Address"))

        assertIs<LocationDisplay.Loading>(vm.state.value.locationDisplay)
    }

    // -----------------------------------------------------------------------
    // Online + geocoder returns address → Address
    // -----------------------------------------------------------------------

    @Test
    fun `when network is available and geocoder returns address, locationDisplay becomes Address`() = runTest {
        val expectedAddress = "Av. Independencia 45, Centro, Guanajuato"
        val vm = buildViewModel(networkAvailable = true, geocodeResult = Result.success(expectedAddress))

        vm.init(listOf(photoWithLocation(GeoLocation(18.4861, -69.9312))))
        advanceUntilIdle()

        val display = vm.state.value.locationDisplay
        assertIs<LocationDisplay.Address>(display)
        assertEquals(expectedAddress, display.text)
    }

    // -----------------------------------------------------------------------
    // Offline → Coordinates immediately
    // -----------------------------------------------------------------------

    @Test
    fun `when network is unavailable, locationDisplay becomes Coordinates`() = runTest {
        val vm = buildViewModel(networkAvailable = false, geocodeResult = Result.success("Should not be used"))

        vm.init(listOf(photoWithLocation(GeoLocation(18.4861, -69.9312))))
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
    fun `when network is available but geocoder returns failure, locationDisplay becomes Coordinates`() = runTest {
        val vm = buildViewModel(
            networkAvailable = true,
            geocodeResult = Result.failure(RuntimeException("Geocoding error"))
        )

        vm.init(listOf(photoWithLocation(GeoLocation(19.4326, -99.1332))))
        advanceUntilIdle()

        val display = vm.state.value.locationDisplay
        assertIs<LocationDisplay.Coordinates>(display)
        assertEquals(19.4326, display.latitude)
        assertEquals(-99.1332, display.longitude)
    }

    // -----------------------------------------------------------------------
    // Online + geocoder returns null address → Coordinates (null treated as failure)
    // -----------------------------------------------------------------------

    @Test
    fun `when network is available and geocoder returns empty string, locationDisplay becomes Address with empty text`() = runTest {
        val vm = buildViewModel(networkAvailable = true, geocodeResult = Result.success(""))

        vm.init(listOf(photoWithLocation(GeoLocation(18.4861, -69.9312))))
        advanceUntilIdle()

        // An empty-string success is still a successful geocode — display it as Address
        val display = vm.state.value.locationDisplay
        assertIs<LocationDisplay.Address>(display)
        assertEquals("", display.text)
    }

    // -----------------------------------------------------------------------
    // Photos with no exifLocation → init exits early, locationDisplay stays Loading
    // -----------------------------------------------------------------------

    @Test
    fun `when all photos have null exifLocation, locationDisplay remains Loading after init`() = runTest {
        val vm = buildViewModel(networkAvailable = true, geocodeResult = Result.success("Ignored"))

        vm.init(listOf(photoWithoutLocation()))
        advanceUntilIdle()

        assertIs<LocationDisplay.Loading>(vm.state.value.locationDisplay)
    }

    // -----------------------------------------------------------------------
    // noLocationOnPhotos state is independent of VM — photos with GPS cause no flag
    // -----------------------------------------------------------------------

    @Test
    fun `passing photos with valid exifLocation does not set any error state`() = runTest {
        val vm = buildViewModel(networkAvailable = true, geocodeResult = Result.success("Some Address"))

        vm.init(listOf(photoWithLocation(GeoLocation(18.0, -69.0))))
        advanceUntilIdle()

        // The form is in a clean submittable state with no error flags
        val state = vm.state.value
        assertEquals(false, state.isSubmitting)
        assertEquals(true, state.showDisclaimer)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildViewModel(
        networkAvailable: Boolean,
        geocodeResult: Result<String>
    ): ReportFormViewModel {
        val fakeGeocodeRepo = FakeGeocodingRepository(geocodeResult)
        val reverseGeocodeUseCase = ReverseGeocodeUseCase(fakeGeocodeRepo)
        return ReportFormViewModel(
            saveReportUseCase = saveReportUseCase,
            reverseGeocodeUseCase = reverseGeocodeUseCase,
            networkAvailable = { networkAvailable }
        )
    }

    private fun photoWithLocation(location: GeoLocation) =
        CapturedPhoto(id = "p1", localPath = "/path/p1.jpg", exifLocation = location)

    private fun photoWithoutLocation() =
        CapturedPhoto(id = "p2", localPath = "/path/p2.jpg", exifLocation = null)
}

private class FakeGeocodingRepository(
    private val result: Result<String>
) : com.espert.reporteciudadano.domain.repository.GeocodingRepository {
    override suspend fun reverseGeocode(location: GeoLocation): Result<String> = result
}
