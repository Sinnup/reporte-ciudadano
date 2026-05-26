package com.espert.reporteciudadano.cloudsync.platform

// JS: SubtleCrypto is async; since SigV4 signing is called synchronously in the data layer
// and Ktor JS is already available for the actual network calls, we implement HMAC-SHA256
// using a pure-Kotlin fallback. For full SubtleCrypto support, the data layer would need
// to be refactored to be fully suspend-based at the signing level.
//
// This implementation uses a pure-Kotlin HMAC-SHA256 to avoid the async boundary issue.
// It is correct but not hardware-accelerated. Replace with SubtleCrypto once the
// data layer is refactored to support async signing.

actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray =
    PureKotlinHmacSha256.compute(key, data)

actual fun sha256Hex(data: String): String = PureKotlinSha256.hex(data.encodeToByteArray())

actual fun sha256Hex(data: ByteArray): String = PureKotlinSha256.hex(data)
