package com.espert.reporteciudadano.cloudsync.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.espert.reporteciudadano.cloudsync.domain.usecase.GetPendingSyncsUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.RecordSyncFailureUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.RetryFailedSyncsUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportResult
import com.espert.reporteciudadano.cloudsync.platform.SyncFailureNotifier
import com.espert.reporteciudadano.domain.repository.ReportRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager CoroutineWorker that orchestrates cloud sync for all pending reports.
 *
 * Returns Result.success() even when individual reports fail — individual failures are
 * tracked in the DB via RecordSyncFailureUseCase. WorkManager retry is used only for
 * infrastructure-level failures (DB unavailable, Koin injection failure, etc.).
 */
class CloudSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val getPendingSyncsUseCase: GetPendingSyncsUseCase by inject()
    private val syncReportUseCase: SyncReportUseCase by inject()
    private val reportRepository: ReportRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val pendingRecords = getPendingSyncsUseCase().getOrElse { e ->
                Log.e(TAG, "getPendingSyncs failed: ${e.message}", e)
                return Result.retry()
            }

            Log.d(TAG, "doWork: ${pendingRecords.size} pending record(s)")

            for (record in pendingRecords) {
                val report = reportRepository.getById(record.reportId).getOrNull() ?: run {
                    Log.w(TAG, "report ${record.reportId} not found in local DB — skipping")
                    continue
                }

                val syncResult = syncReportUseCase(report)
                syncResult.onFailure { e ->
                    Log.e(TAG, "syncReportUseCase threw for ${report.id}: ${e.message}", e)
                }
                val result = syncResult.getOrNull() ?: continue

                when (result) {
                    is SyncReportResult.Success ->
                        Log.d(TAG, "synced ${report.id} successfully")
                    is SyncReportResult.Failed -> {
                        Log.w(TAG, "sync failed for ${report.id} threshold=${result.thresholdReached}")
                        if (result.thresholdReached) SyncFailureNotifier.notifySyncFailure(report.id, report.title)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork threw unexpectedly: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "CloudSyncWorker"
    }
}
