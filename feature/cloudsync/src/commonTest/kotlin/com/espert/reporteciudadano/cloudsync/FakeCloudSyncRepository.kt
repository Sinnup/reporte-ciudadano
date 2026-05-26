package com.espert.reporteciudadano.cloudsync

import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.SyncRecord
import com.espert.reporteciudadano.domain.model.SyncStatus

/**
 * In-memory fake for [CloudSyncRepository].
 * All operations are configurable to succeed or fail via the [syncReportResult] / [syncPhotoResult] fields.
 */
class FakeCloudSyncRepository : CloudSyncRepository {

    // Configurable responses
    var syncReportResult: Result<Unit> = Result.success(Unit)
    var syncPhotoResult: Result<Unit> = Result.success(Unit)

    // Records of calls
    val syncedReportIds = mutableListOf<String>()
    val syncedPhotoPaths = mutableListOf<Pair<String, String>>()
    val markedSyncedIds = mutableListOf<String>()
    val recordedFailureIds = mutableListOf<String>()
    val resetForRetryIds = mutableListOf<String>()

    // In-memory state
    private val failureCounts = mutableMapOf<String, Int>()
    private val syncedAt = mutableMapOf<String, Long?>()

    fun seedRecord(reportId: String, failureCount: Int, syncedAtMillis: Long? = null) {
        failureCounts[reportId] = failureCount
        syncedAt[reportId] = syncedAtMillis
    }

    override suspend fun syncReport(report: CitizenReport): Result<Unit> {
        syncedReportIds.add(report.id)
        return syncReportResult
    }

    override suspend fun syncPhoto(reportId: String, localPath: String): Result<Unit> {
        syncedPhotoPaths.add(reportId to localPath)
        return syncPhotoResult
    }

    override suspend fun getPendingSyncReports(): Result<List<SyncRecord>> = runCatching {
        (failureCounts.keys + syncedAt.keys).toSet().mapNotNull { id ->
            val count = failureCounts[id] ?: 0
            if (count >= MAX_CONSECUTIVE_FAILURES) return@mapNotNull null
            val at = syncedAt[id]
            val status = when {
                at != null && count == 0 -> SyncStatus.SYNCED
                count > 0 -> SyncStatus.FAILED
                else -> SyncStatus.PENDING
            }
            SyncRecord(reportId = id, syncStatus = status, syncedAt = at, syncFailureCount = count)
        }
    }

    override suspend fun markSynced(reportId: String): Result<Unit> {
        markedSyncedIds.add(reportId)
        failureCounts[reportId] = 0
        syncedAt[reportId] = System.currentTimeMillis()
        return Result.success(Unit)
    }

    override suspend fun recordSyncFailure(reportId: String): Result<Unit> {
        recordedFailureIds.add(reportId)
        failureCounts[reportId] = (failureCounts[reportId] ?: 0) + 1
        return Result.success(Unit)
    }

    override suspend fun resetForRetry(reportId: String): Result<Unit> {
        resetForRetryIds.add(reportId)
        failureCounts[reportId] = 0
        syncedAt[reportId] = null
        return Result.success(Unit)
    }
}
