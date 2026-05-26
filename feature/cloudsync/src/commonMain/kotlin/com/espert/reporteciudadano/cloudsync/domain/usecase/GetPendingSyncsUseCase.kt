package com.espert.reporteciudadano.cloudsync.domain.usecase

import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.domain.model.SyncRecord

/**
 * Returns all [SyncRecord]s that are pending or have failed
 * and have not yet reached the failure threshold.
 * Used by the platform scheduler on all targets to determine if sync work is needed.
 */
class GetPendingSyncsUseCase(private val cloudSyncRepository: CloudSyncRepository) {
    suspend operator fun invoke(): Result<List<SyncRecord>> =
        cloudSyncRepository.getPendingSyncReports()
}
