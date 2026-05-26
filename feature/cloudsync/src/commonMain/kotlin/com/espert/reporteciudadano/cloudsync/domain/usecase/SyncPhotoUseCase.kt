package com.espert.reporteciudadano.cloudsync.domain.usecase

import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository

/**
 * Thin wrapper around [CloudSyncRepository.syncPhoto].
 * Exists so photo uploads can be tested and retried independently of the full report sync.
 */
class SyncPhotoUseCase(private val cloudSyncRepository: CloudSyncRepository) {
    suspend operator fun invoke(reportId: String, localPath: String): Result<Unit> =
        cloudSyncRepository.syncPhoto(reportId, localPath)
}
