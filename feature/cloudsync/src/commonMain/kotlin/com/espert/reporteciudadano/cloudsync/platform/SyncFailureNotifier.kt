@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

/**
 * Platform-specific failure notification dispatcher.
 *
 * Android: NotificationManager + SyncRetryReceiver BroadcastReceiver
 * iOS: UNUserNotificationCenter with a RETRY_SYNC action
 * JS/WasmJS: Posts to a SharedFlow consumed by the root Scaffold's SnackbarHost
 */
expect object SyncFailureNotifier {
    fun notifySyncFailure(reportId: String, reportTitle: String)
    fun cancelNotification(reportId: String)
}
