package com.espert.reporteciudadano.domain.model

/**
 * Tracks the cloud synchronisation state of a single [CitizenReport].
 *
 * Maps directly to the [synced_at] and [sync_failure_count] columns
 * added to ReportEntity in schema version 3.
 *
 * Never exposed to the presentation layer — sync state is a background concern.
 */
data class SyncRecord(
    val reportId: String,
    val syncStatus: SyncStatus,
    /** Epoch millis; null if the report has never been successfully synced. */
    val syncedAt: Long?,
    /** Resets to 0 on every successful sync. */
    val syncFailureCount: Int
)
