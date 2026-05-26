package com.espert.reporteciudadano.cloudsync.domain.repository

import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.SyncRecord

/** Maximum consecutive upload failures before a failure notification is fired. */
const val MAX_CONSECUTIVE_FAILURES = 5

interface CloudSyncRepository {

    /**
     * Upload a single report's metadata to DynamoDB.
     * Idempotent: uses conditional write (attribute_not_exists(id)).
     * A ConditionalCheckFailedException is treated as success.
     */
    suspend fun syncReport(report: CitizenReport): Result<Unit>

    /**
     * Upload a single photo file to S3.
     * Idempotent: performs HeadObject before PutObject; skips if object already exists.
     * S3 key format: reports/<reportId>/<localFilename>
     */
    suspend fun syncPhoto(reportId: String, localPath: String): Result<Unit>

    /**
     * Retrieve all [SyncRecord]s whose status is PENDING or FAILED
     * and whose syncFailureCount < [MAX_CONSECUTIVE_FAILURES].
     */
    suspend fun getPendingSyncReports(): Result<List<SyncRecord>>

    /**
     * Mark a report sync as succeeded.
     * Sets synced_at to current epoch millis, resets sync_failure_count to 0.
     */
    suspend fun markSynced(reportId: String): Result<Unit>

    /**
     * Increment the failure count for a report.
     * If count reaches [MAX_CONSECUTIVE_FAILURES], the status is effectively FAILED.
     */
    suspend fun recordSyncFailure(reportId: String): Result<Unit>

    /**
     * Reset sync_failure_count to 0 for a report.
     * Used when the user taps the retry notification.
     */
    suspend fun resetForRetry(reportId: String): Result<Unit>
}
