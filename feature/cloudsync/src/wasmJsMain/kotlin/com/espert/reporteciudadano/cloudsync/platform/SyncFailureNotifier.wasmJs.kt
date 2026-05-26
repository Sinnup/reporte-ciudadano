@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

// WasmJS: Cloud sync is a no-op; failure notification is never triggered.
actual object SyncFailureNotifier {
    actual fun notifySyncFailure(reportId: String, reportTitle: String) {
        // No-op: cloud sync is not supported on WasmJS
    }

    actual fun cancelNotification(reportId: String) {
        // No-op
    }
}
