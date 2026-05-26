package com.espert.reporteciudadano.cloudsync.platform

// WasmJS stub: same approach as jsMain — credentials would be injected at build time.
// Cloud sync is a no-op on WasmJS (Ktor has no WasmJS engine in 3.1.3), so
// this will never be called in practice; the CloudSyncRepositoryImpl WasmJS
// implementation returns Result.failure(UnsupportedOperationException(...)) immediately.
actual fun loadAwsCredentials(): AwsCredentials {
    throw IllegalStateException(
        "AWS credentials are not configured for the WasmJS platform. " +
        "Cloud sync is currently unsupported on WasmJS (no Ktor WasmJS engine in 3.1.3)."
    )
}
