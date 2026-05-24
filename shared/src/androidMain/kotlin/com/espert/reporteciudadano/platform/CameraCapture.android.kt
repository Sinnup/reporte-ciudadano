package com.espert.reporteciudadano.platform

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
import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.navigation.CapturedPhoto
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume

@Composable
actual fun CameraCapture(onPhotoTaken: (CapturedPhoto) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photoFile by remember { mutableStateOf<File?>(null) }
    // Deferred so the camera callback can await the GPS fix if it hasn't arrived yet
    var locationDeferred by remember { mutableStateOf<Deferred<GeoLocation?>?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = photoFile ?: return@rememberLauncherForActivityResult
            scope.launch {
                val exifLocation = readExifLocation(file.absolutePath)
                // Prefer EXIF; otherwise wait up to 15 s for the device-location coroutine
                val location = exifLocation ?: withTimeoutOrNull(15_000L) { locationDeferred?.await() }
                onPhotoTaken(CapturedPhoto(UUID.randomUUID().toString(), file.absolutePath, location))
            }
        } else {
            onCancel()
        }
    }

    LaunchedEffect(Unit) {
        locationDeferred = async { getCurrentDeviceLocation(context) }
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

        // Slow path: register all enabled providers so GPS and network race — first fix wins
        val listener = LocationListener { loc ->
            if (continuation.isActive) continuation.resume(GeoLocation(loc.latitude, loc.longitude))
        }
        val registered = providers.count { provider ->
            runCatching {
                lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }.isSuccess
        }
        if (registered == 0) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        continuation.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
    }
