package com.espert.reporteciudadano.feature.camera

import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.navigation.CapturedPhoto
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CameraViewModelTest {

    private lateinit var viewModel: CameraViewModel

    @BeforeTest
    fun setUp() {
        viewModel = CameraViewModel()
    }

    // -----------------------------------------------------------------------
    // Initial state
    // -----------------------------------------------------------------------

    @Test
    fun `noLocationOnPhotos is false in the initial state`() {
        assertFalse(viewModel.state.value.noLocationOnPhotos)
    }

    @Test
    fun `photos list is empty in the initial state`() {
        assertTrue(viewModel.state.value.photos.isEmpty())
    }

    // -----------------------------------------------------------------------
    // PhotoTaken → noLocationOnPhotos stays false when EXIF location present
    // -----------------------------------------------------------------------

    @Test
    fun `taking a photo with valid exifLocation keeps noLocationOnPhotos false`() {
        val photo = capturedPhotoWithLocation("p1", GeoLocation(18.4861, -69.9312))

        viewModel.processIntent(CameraIntent.PhotoTaken(photo))

        assertFalse(viewModel.state.value.noLocationOnPhotos)
        assertEquals(1, viewModel.state.value.photos.size)
    }

    // -----------------------------------------------------------------------
    // NoLocationOnPhotos intent
    // -----------------------------------------------------------------------

    @Test
    fun `NoLocationOnPhotos intent sets noLocationOnPhotos to true`() {
        viewModel.processIntent(CameraIntent.PhotoTaken(capturedPhotoWithoutLocation("p1")))

        viewModel.processIntent(CameraIntent.NoLocationOnPhotos)

        assertTrue(viewModel.state.value.noLocationOnPhotos)
    }

    // -----------------------------------------------------------------------
    // DeletePhoto — when remaining photos have GPS, noLocationOnPhotos clears
    // -----------------------------------------------------------------------

    @Test
    fun `deleting a GPS-less photo when remaining photos have GPS clears noLocationOnPhotos`() {
        val photoNoGps = capturedPhotoWithoutLocation("no-gps")
        val photoWithGps = capturedPhotoWithLocation("with-gps", GeoLocation(18.4861, -69.9312))
        viewModel.processIntent(CameraIntent.PhotoTaken(photoNoGps))
        viewModel.processIntent(CameraIntent.PhotoTaken(photoWithGps))
        viewModel.processIntent(CameraIntent.NoLocationOnPhotos)
        assertTrue(viewModel.state.value.noLocationOnPhotos)

        viewModel.processIntent(CameraIntent.DeletePhoto(photoNoGps.id))

        assertFalse(viewModel.state.value.noLocationOnPhotos)
    }

    // -----------------------------------------------------------------------
    // DeletePhoto — when ALL remaining photos have no GPS, flag stays true
    // -----------------------------------------------------------------------

    @Test
    fun `deleting a GPS-bearing photo when all remaining have no GPS keeps noLocationOnPhotos true`() {
        val photoWithGps = capturedPhotoWithLocation("with-gps", GeoLocation(18.4861, -69.9312))
        val photoNoGps = capturedPhotoWithoutLocation("no-gps")
        viewModel.processIntent(CameraIntent.PhotoTaken(photoWithGps))
        viewModel.processIntent(CameraIntent.PhotoTaken(photoNoGps))
        viewModel.processIntent(CameraIntent.NoLocationOnPhotos)

        viewModel.processIntent(CameraIntent.DeletePhoto(photoWithGps.id))

        assertTrue(viewModel.state.value.noLocationOnPhotos)
    }

    @Test
    fun `deleting a photo reduces the photo count`() {
        val photo = capturedPhotoWithLocation("p1", GeoLocation(0.0, 0.0))
        viewModel.processIntent(CameraIntent.PhotoTaken(photo))
        assertEquals(1, viewModel.state.value.photos.size)

        viewModel.processIntent(CameraIntent.DeletePhoto(photo.id))

        assertEquals(0, viewModel.state.value.photos.size)
    }

    // -----------------------------------------------------------------------
    // LocationOnPhotosClear intent
    // -----------------------------------------------------------------------

    @Test
    fun `LocationOnPhotosClear intent resets noLocationOnPhotos to false`() {
        viewModel.processIntent(CameraIntent.NoLocationOnPhotos)
        assertTrue(viewModel.state.value.noLocationOnPhotos)

        viewModel.processIntent(CameraIntent.LocationOnPhotosClear)

        assertFalse(viewModel.state.value.noLocationOnPhotos)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun capturedPhotoWithLocation(id: String, location: GeoLocation) =
        CapturedPhoto(id = id, localPath = "/path/$id.jpg", exifLocation = location)

    private fun capturedPhotoWithoutLocation(id: String) =
        CapturedPhoto(id = id, localPath = "/path/$id.jpg", exifLocation = null)
}
