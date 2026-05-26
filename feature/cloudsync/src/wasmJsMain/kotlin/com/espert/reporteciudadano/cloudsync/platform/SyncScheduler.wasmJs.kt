@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

// WasmJS: Cloud sync is not supported (no Ktor WasmJS engine in ktor 3.1.3).
// All scheduler methods are no-ops.
actual object SyncScheduler {
    actual fun scheduleBackgroundSync() {
        // No-op: WasmJS sync is unsupported
    }

    actual fun cancelBackgroundSync() {
        // No-op
    }

    actual fun scheduleEagerSync() {
        // No-op: WasmJS sync is unsupported
    }
}
