@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

import com.espert.reporteciudadano.cloudsync.domain.usecase.GetPendingSyncsUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportResult
import com.espert.reporteciudadano.cloudsync.platform.SyncFailureNotifier
import com.espert.reporteciudadano.domain.repository.ReportRepository
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.BackgroundTasks.*

private const val SYNC_TASK_IDENTIFIER = "com.espert.reporteciudadano.cloudsync"

actual object SyncScheduler : KoinComponent {
    private val getPendingSyncsUseCase: GetPendingSyncsUseCase by inject()
    private val syncReportUseCase: SyncReportUseCase by inject()
    private val reportRepository: ReportRepository by inject()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    actual fun scheduleBackgroundSync() {
        val request = BGProcessingTaskRequest(SYNC_TASK_IDENTIFIER)
        request.requiresNetworkConnectivity = true
        runCatching { BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null) }
    }

    actual fun cancelBackgroundSync() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(SYNC_TASK_IDENTIFIER)
    }

    actual fun scheduleEagerSync() {
        scope.launch { runForegroundSync() }
    }

    private suspend fun runForegroundSync() {
        val pendingRecords = getPendingSyncsUseCase().getOrElse { return }
        for (record in pendingRecords) {
            val report = reportRepository.getById(record.reportId).getOrNull() ?: continue
            val result = syncReportUseCase(report).getOrNull() ?: continue
            if (result is SyncReportResult.Failed && result.thresholdReached) {
                SyncFailureNotifier.notifySyncFailure(report.id, report.title)
            }
        }
    }
}
