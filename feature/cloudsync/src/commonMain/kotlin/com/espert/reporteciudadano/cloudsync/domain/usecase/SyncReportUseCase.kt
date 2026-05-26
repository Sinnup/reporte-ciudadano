package com.espert.reporteciudadano.cloudsync.domain.usecase

import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.domain.model.CitizenReport

/**
 * Orchestrates a full sync for one report:
 * 1. Uploads report metadata to DynamoDB.
 * 2. Uploads each photo to S3.
 * 3. On any failure, delegates to [RecordSyncFailureUseCase].
 * 4. On full success, marks the report as synced.
 *
 * Returns Result.success(true) if all uploads succeeded.
 * Returns Result.success(false) if a failure was recorded (threshold may have been reached).
 * Returns Result.failure on infrastructure-level errors.
 */
class SyncReportUseCase(
    private val cloudSyncRepository: CloudSyncRepository,
    private val recordSyncFailureUseCase: RecordSyncFailureUseCase
) {
    suspend operator fun invoke(report: CitizenReport): Result<SyncReportResult> = runCatching {
        val reportResult = cloudSyncRepository.syncReport(report)
        if (reportResult.isFailure) {
            val failure = recordSyncFailureUseCase(report.id).getOrDefault(RecordSyncFailureResult(0, false))
            return Result.success(SyncReportResult.Failed(failure.thresholdReached))
        }

        var anyPhotoFailed = false
        for (photo in report.photos) {
            val photoResult = cloudSyncRepository.syncPhoto(report.id, photo.localPath)
            if (photoResult.isFailure) {
                anyPhotoFailed = true
            }
        }

        if (anyPhotoFailed) {
            val failure = recordSyncFailureUseCase(report.id).getOrDefault(RecordSyncFailureResult(0, false))
            return Result.success(SyncReportResult.Failed(failure.thresholdReached))
        }

        cloudSyncRepository.markSynced(report.id)
        SyncReportResult.Success
    }
}

sealed interface SyncReportResult {
    data object Success : SyncReportResult
    data class Failed(val thresholdReached: Boolean) : SyncReportResult
}
