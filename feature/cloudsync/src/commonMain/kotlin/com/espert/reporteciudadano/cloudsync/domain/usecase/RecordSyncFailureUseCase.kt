package com.espert.reporteciudadano.cloudsync.domain.usecase

import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES

/**
 * Records a sync failure for a report and indicates whether the failure threshold has been reached.
 *
 * The caller (platform Worker/task) is responsible for dispatching a failure notification
 * when [RecordSyncFailureResult.thresholdReached] is true.
 */
class RecordSyncFailureUseCase(private val cloudSyncRepository: CloudSyncRepository) {
    suspend operator fun invoke(reportId: String): Result<RecordSyncFailureResult> =
        cloudSyncRepository.recordSyncFailure(reportId).mapCatching {
            // After recording, check the new failure count by fetching the sync record
            val records = cloudSyncRepository.getPendingSyncReports().getOrElse { emptyList() }
            val record = records.firstOrNull { it.reportId == reportId }
            val count = record?.syncFailureCount ?: MAX_CONSECUTIVE_FAILURES
            RecordSyncFailureResult(
                failureCount = count,
                thresholdReached = count >= MAX_CONSECUTIVE_FAILURES
            )
        }
}

data class RecordSyncFailureResult(
    val failureCount: Int,
    val thresholdReached: Boolean
)
