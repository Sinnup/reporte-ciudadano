@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

import com.espert.reporteciudadano.cloudsync.domain.usecase.GetPendingSyncsUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportUseCase
import com.espert.reporteciudadano.cloudsync.domain.usecase.SyncReportResult
import com.espert.reporteciudadano.domain.repository.ReportRepository
import kotlinx.browser.window
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object SyncScheduler : KoinComponent {
    private val getPendingSyncsUseCase: GetPendingSyncsUseCase by inject()
    private val syncReportUseCase: SyncReportUseCase by inject()
    private val reportRepository: ReportRepository by inject()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    actual fun scheduleBackgroundSync() {
        // Register an "online" event listener to trigger sync when the browser comes back online
        window.addEventListener("online") {
            scheduleEagerSync()
        }
        // Run immediately if already online
        if (window.navigator.onLine) {
            scheduleEagerSync()
        }
    }

    actual fun cancelBackgroundSync() {
        // No persistent scheduler to cancel on JS; the online listener persists for the page lifetime
    }

    actual fun scheduleEagerSync() {
        scope.launch { runForegroundSync() }
    }

    private suspend fun runForegroundSync() {
        if (!window.navigator.onLine) return
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
