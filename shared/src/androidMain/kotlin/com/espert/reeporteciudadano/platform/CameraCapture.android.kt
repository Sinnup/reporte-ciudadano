package com.espert.reeporteciudadano.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.espert.reeporteciudadano.domain.model.GeoLocation
import com.espert.reeporteciudadano.navigation.CapturedPhoto
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume

@Composable
actual fun CameraCapture(onPhotoTaken: (CapturedPhoto) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var photoFile by remember { mutableStateOf<File?>(null) }
    var deviceLocation by remember { mutableStateOf<GeoLocation?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = photoFile ?: return@rememberLauncherForActivityResult
            val location = readExifLocation(file.absolutePath) ?: deviceLocation
            onPhotoTaken(CapturedPhoto(UUID.randomUUID().toString(), file.absolutePath, location))
        } else {
            onCancel()
        }
    }

    LaunchedEffect(Unit) {
        // Fetch device location in parallel while the user takes the photo
        launch { deviceLocation = getCurrentDeviceLocation(context) }

        val file = createImageFile(context)
        photoFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        launcher.launch(uri)
    }
}

private fun createImageFile(context: Context): File {
    val dir = File(context.cacheDir, "camera").also { it.mkdirs() }
    return File(dir, "photo_${UUID.randomUUID()}.jpg")
}

private fun readExifLocation(path: String): GeoLocation? = try {
    val exif = ExifInterface(path)
    val latLon = FloatArray(2)
    if (exif.getLatLong(latLon)) GeoLocation(latLon[0].toDouble(), latLon[1].toDouble()) else null
} catch (_: Exception) { null }

@SuppressLint("MissingPermission")
private suspend fun getCurrentDeviceLocation(context: Context): GeoLocation? =
    suspendCancellableCoroutine { continuation ->
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }

        if (providers.isEmpty()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        // Fast path: use any cached fix immediately
        providers.firstNotNullOfOrNull {
            runCatching { lm.getLastKnownLocation(it) }.getOrNull()
        }?.let {
            continuation.resume(GeoLocation(it.latitude, it.longitude))
            return@suspendCancellableCoroutine
        }

        // Slow path: register for the first available fix (network provider is fastest)
        val listener = LocationListener { loc ->
            if (continuation.isActive) continuation.resume(GeoLocation(loc.latitude, loc.longitude))
        }
        runCatching {
            lm.requestLocationUpdates(providers.first(), 0L, 0f, listener, Looper.getMainLooper())
        }.onFailure {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        continuation.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
    }
