package com.espert.reporteciudadano.domain.repository

import com.espert.reporteciudadano.domain.model.SyncStatus

/**
 * Read-only sync state access for the presentation layer.
 *
 * This minimal interface lives in `:shared` so that `MyReportsViewModel` can consume it
 * without creating a circular dependency on `:feature:cloudsync`.
 * The implementation is provided by `CloudSyncRepositoryImpl` in `:feature:cloudsync`
 * and registered in Koin from `cloudSyncModule`.
 */
interface SyncStateRepository {
    /**
     * Returns a map of reportId → SyncStatus for all known reports.
     * Reports not present in the map should be treated as [SyncStatus.PENDING].
     */
    suspend fun getSyncStates(): Result<Map<String, SyncStatus>>
}
