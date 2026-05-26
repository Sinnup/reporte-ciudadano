package com.espert.reporteciudadano.cloudsync.platform

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(key, "HmacSHA256"))
    return mac.doFinal(data)
}

actual fun sha256Hex(data: String): String =
    MessageDigest.getInstance("SHA-256").digest(data.encodeToByteArray()).toHex()

actual fun sha256Hex(data: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(data).toHex()
