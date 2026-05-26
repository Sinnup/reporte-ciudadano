@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event data for a sync failure notification on the Web platform.
 * Collected by the root composable's Snackbar to show an in-app retry prompt.
 */
data class SyncFailureEvent(
    val reportId: String,
    val reportTitle: String
)

private val _syncFailureEvents = MutableSharedFlow<SyncFailureEvent>(extraBufferCapacity = 8)

/**
 * SharedFlow consumed by the Web root Scaffold's SnackbarHost.
 *
 * Wire this in the root composable:
 * ```kotlin
 * LaunchedEffect(Unit) {
 *     SyncFailureNotifier.syncFailureEvents.collect { event ->
 *         val result = snackbarHostState.showSnackbar(
 *             message = "Sync failed for \"${event.reportTitle}\". Reopen the app when you are online to retry.",
 *             actionLabel = "Retry Sync",
 *             duration = SnackbarDuration.Indefinite
 *         )
 *         if (result == SnackbarResult.ActionPerformed) {
 *             retryFailedSyncsUseCase(event.reportId)
 *         }
 *     }
 * }
 * ```
 */
val syncFailureEventsFlow: SharedFlow<SyncFailureEvent> = _syncFailureEvents.asSharedFlow()

actual object SyncFailureNotifier {
    actual fun notifySyncFailure(reportId: String, reportTitle: String) {
        _syncFailureEvents.tryEmit(SyncFailureEvent(reportId, reportTitle))
    }

    actual fun cancelNotification(reportId: String) {
        // No persistent notification to cancel on JS; the Snackbar auto-dismisses
    }
}
