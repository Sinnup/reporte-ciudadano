package com.espert.reporteciudadano.cloudsync.worker

import android.content.Context
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
            val pendingRecords = getPendingSyncsUseCase().getOrElse {
                return Result.retry() // DB or infrastructure failure
            }

            for (record in pendingRecords) {
                val report = reportRepository.getById(record.reportId).getOrNull() ?: continue

                val syncResult = syncReportUseCase(report).getOrNull() ?: continue

                if (syncResult is SyncReportResult.Failed && syncResult.thresholdReached) {
                    SyncFailureNotifier.notifySyncFailure(report.id, report.title)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
