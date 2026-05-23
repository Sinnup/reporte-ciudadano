package com.espert.reeporteciudadano.platform

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.espert.reeporteciudadano.domain.model.GeoLocation
import com.espert.reeporteciudadano.navigation.CapturedPhoto
import java.io.File
import java.util.UUID

@Composable
actual fun CameraCapture(onPhotoTaken: (CapturedPhoto) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = photoFile ?: return@rememberLauncherForActivityResult
            val location = readExifLocation(file.absolutePath)
            onPhotoTaken(CapturedPhoto(UUID.randomUUID().toString(), file.absolutePath, location))
        } else {
            onCancel()
        }
    }

    LaunchedEffect(Unit) {
        val file = createImageFile(context)
        photoFile = file
        photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        photoUri?.let { launcher.launch(it) }
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
} catch (e: Exception) { null }
