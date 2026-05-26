@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

/**
 * Platform-specific sync scheduler.
 *
 * Android: WorkManager PeriodicWorkRequest + OneTimeWorkRequest
 * iOS: BGTaskScheduler (BGProcessingTaskRequest)
 * JS/WasmJS: window.addEventListener("online", ...) + inline coroutine
 */
expect object SyncScheduler {
    /** Register a periodic background sync (WorkManager periodic / BGTaskScheduler submit). */
    fun scheduleBackgroundSync()

    /** Cancel any pending background sync work. */
    fun cancelBackgroundSync()

    /**
     * Enqueue an immediate one-time sync.
     * Android: expedited OneTimeWorkRequest
     * iOS: foreground coroutine
     * JS/WasmJS: inline coroutine if online
     */
    fun scheduleEagerSync()
}
