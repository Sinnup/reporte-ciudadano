package com.espert.reporteciudadano.cloudsync.domain.usecase

import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES
import com.espert.reporteciudadano.cloudsync.platform.SyncScheduler
import com.espert.reporteciudadano.domain.model.SyncStatus

/**
 * Resets sync_failure_count to 0 for all FAILED reports that have reached the threshold,
 * then re-enqueues the platform sync scheduler so they will be retried.
 *
 * Called from the notification retry path (SyncRetryReceiver on Android,
 * notification action handler on iOS, Snackbar retry on Web).
 *
 * @param scheduleEagerSync Injectable scheduler trigger; defaults to [SyncScheduler.scheduleEagerSync].
 *   Provided as a lambda so unit tests can substitute a no-op without triggering platform code.
 */
class RetryFailedSyncsUseCase(
    private val cloudSyncRepository: CloudSyncRepository,
    private val getPendingSyncsUseCase: GetPendingSyncsUseCase,
    private val scheduleEagerSync: () -> Unit = { SyncScheduler.scheduleEagerSync() }
) {
    suspend operator fun invoke() {
        // Fetch all records, including those at or above the threshold
        val allRecords = cloudSyncRepository.getPendingSyncReports().getOrElse { emptyList() }
        allRecords
            .filter { it.syncStatus == SyncStatus.FAILED && it.syncFailureCount >= MAX_CONSECUTIVE_FAILURES }
            .forEach { record ->
                cloudSyncRepository.resetForRetry(record.reportId)
            }
        scheduleEagerSync()
    }

    /** Retry a specific report by ID only. */
    suspend operator fun invoke(reportId: String) {
        cloudSyncRepository.resetForRetry(reportId)
        scheduleEagerSync()
    }
}
