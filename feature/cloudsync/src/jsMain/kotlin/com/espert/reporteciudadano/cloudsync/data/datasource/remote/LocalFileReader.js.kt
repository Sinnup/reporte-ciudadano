package com.espert.reporteciudadano.cloudsync.data.datasource.remote

// JS does not have access to the local filesystem.
// Photo upload on the JS platform would require a different mechanism (e.g., Blob/File API).
// For now, this returns null to skip photo uploads gracefully.
actual class LocalFileReader actual constructor() {
    actual fun readBytes(path: String): ByteArray? = null
}
