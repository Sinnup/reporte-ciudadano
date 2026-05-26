package com.espert.reporteciudadano.cloudsync.platform

// WasmJS: Cloud sync is a no-op on WasmJS (no Ktor WasmJS engine in ktor 3.1.3).
// These actuals provide the pure-Kotlin fallback so the module compiles.
// In practice CloudSyncRepositoryImpl on WasmJS returns
// Result.failure(UnsupportedOperationException(...)) before these are ever invoked.
actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray =
    PureKotlinHmacSha256.compute(key, data)

actual fun sha256Hex(data: String): String =
    PureKotlinSha256.hex(data.encodeToByteArray())
