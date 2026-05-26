package com.espert.reporteciudadano.cloudsync.data.repository

import com.espert.reporteciudadano.cloudsync.data.datasource.remote.DynamoDbDataSource
import com.espert.reporteciudadano.cloudsync.data.datasource.remote.S3DataSource
import com.espert.reporteciudadano.cloudsync.domain.repository.CloudSyncRepository
import com.espert.reporteciudadano.cloudsync.domain.repository.MAX_CONSECUTIVE_FAILURES
import com.espert.reporteciudadano.database.AppDatabase
import com.espert.reporteciudadano.domain.model.CitizenReport
import com.espert.reporteciudadano.domain.model.SyncRecord
import com.espert.reporteciudadano.domain.model.SyncStatus
import com.espert.reporteciudadano.domain.repository.SyncStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class CloudSyncRepositoryImpl(
    private val dynamoDbDataSource: DynamoDbDataSource,
    private val s3DataSource: S3DataSource,
    private val db: AppDatabase
) : CloudSyncRepository, SyncStateRepository {

    override suspend fun syncReport(report: CitizenReport): Result<Unit> =
        dynamoDbDataSource.putReport(report)

    override suspend fun syncPhoto(reportId: String, localPath: String): Result<Unit> =
        s3DataSource.putPhoto(reportId, localPath)

    override suspend fun getPendingSyncReports(): Result<List<SyncRecord>> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.appDatabaseQueries.getAllPendingSync().executeAsList().mapNotNull { row ->
                    val failureCount = row.sync_failure_count?.toInt() ?: 0
                    // Only return records that can still be retried
                    if (failureCount >= MAX_CONSECUTIVE_FAILURES) return@mapNotNull null
                    val syncedAt = row.synced_at
                    val status = when {
                        syncedAt != null && failureCount == 0 -> SyncStatus.SYNCED
                        failureCount > 0 -> SyncStatus.FAILED
                        else -> SyncStatus.PENDING
                    }
                    SyncRecord(
                        reportId = row.id,
                        syncStatus = status,
                        syncedAt = syncedAt,
                        syncFailureCount = failureCount
                    )
                }
            }
        }

    override suspend fun markSynced(reportId: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.appDatabaseQueries.markReportSynced(
                    synced_at = Clock.System.now().toEpochMilliseconds(),
                    id = reportId
                )
                Unit
            }
        }

    override suspend fun recordSyncFailure(reportId: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.appDatabaseQueries.incrementSyncFailure(reportId)
                Unit
            }
        }

    override suspend fun resetForRetry(reportId: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.appDatabaseQueries.resetSyncForRetry(reportId)
                Unit
            }
        }

    override suspend fun getSyncStates(): Result<Map<String, SyncStatus>> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.appDatabaseQueries.getAllPendingSync().executeAsList().associate { row ->
                    val failureCount = row.sync_failure_count?.toInt() ?: 0
                    val syncedAt = row.synced_at
                    val status = when {
                        syncedAt != null && failureCount == 0 -> SyncStatus.SYNCED
                        failureCount > 0 -> SyncStatus.FAILED
                        else -> SyncStatus.PENDING
                    }
                    row.id to status
                }
            }
        }
}
