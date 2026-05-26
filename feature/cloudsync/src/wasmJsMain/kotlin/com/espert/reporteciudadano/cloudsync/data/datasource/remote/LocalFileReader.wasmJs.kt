package com.espert.reporteciudadano.cloudsync.data.datasource.remote

// WasmJS: Cloud sync is a no-op; file reading is not needed.
actual class LocalFileReader actual constructor() {
    actual fun readBytes(path: String): ByteArray? = null
}
