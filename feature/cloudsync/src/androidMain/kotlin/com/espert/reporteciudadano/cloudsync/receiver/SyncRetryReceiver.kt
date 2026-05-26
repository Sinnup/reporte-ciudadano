package com.espert.reporteciudadano.cloudsync.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.espert.reporteciudadano.cloudsync.domain.usecase.RetryFailedSyncsUseCase
import com.espert.reporteciudadano.cloudsync.platform.SyncScheduler
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val EXTRA_REPORT_ID = "reportId"

/**
 * BroadcastReceiver triggered when the user taps "Retry Sync" in the failure notification.
 * Resets the failure count for the given report and re-enqueues a sync via WorkManager.
 */
class SyncRetryReceiver : BroadcastReceiver(), KoinComponent {
    private val retryFailedSyncsUseCase: RetryFailedSyncsUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val reportId = intent.getStringExtra(EXTRA_REPORT_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                retryFailedSyncsUseCase(reportId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
