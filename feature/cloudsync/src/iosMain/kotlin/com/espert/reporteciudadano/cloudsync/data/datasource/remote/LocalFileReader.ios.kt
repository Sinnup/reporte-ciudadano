package com.espert.reporteciudadano.cloudsync.data.datasource.remote

import kotlinx.cinterop.*
import platform.Foundation.*

actual class LocalFileReader actual constructor() {
    actual fun readBytes(path: String): ByteArray? {
        val url = NSURL.fileURLWithPath(path)
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        return ByteArray(data.length.toInt()).also { bytes ->
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    }
}
