package com.espert.reporteciudadano.cloudsync.data.datasource.remote

import java.io.File

actual class LocalFileReader actual constructor() {
    actual fun readBytes(path: String): ByteArray? =
        runCatching { File(path).readBytes() }.getOrNull()
}
