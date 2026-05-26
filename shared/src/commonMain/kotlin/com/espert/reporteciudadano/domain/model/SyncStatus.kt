package com.espert.reporteciudadano.domain.model

/**
 * Represents the cloud synchronisation state of a single CitizenReport.
 *
 * This is an operational concern — it is independent of [ReportStatus],
 * which describes the civic lifecycle of the report (SENT, SEEN, IN_PROGRESS, etc.).
 */
enum class SyncStatus {
    /** Report has not yet been attempted for upload. */
    PENDING,

    /** Sync is actively running for this report. */
    IN_PROGRESS,

    /** Report was successfully uploaded to DynamoDB and all photos to S3. */
    SYNCED,

    /** At least one upload attempt has failed. */
    FAILED
}
