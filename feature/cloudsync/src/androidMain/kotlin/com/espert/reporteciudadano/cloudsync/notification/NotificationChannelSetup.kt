package com.espert.reporteciudadano.cloudsync.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

const val SYNC_FAILURES_CHANNEL_ID = "sync_failures"

/**
 * Creates the sync_failures notification channel.
 * Call from Application.onCreate() or the Koin androidMain module's setup block.
 */
object NotificationChannelSetup {
    fun createSyncFailuresChannel(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(SYNC_FAILURES_CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            SYNC_FAILURES_CHANNEL_ID,
            "Sync Failures",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when a report could not be uploaded to the city server after repeated attempts."
        }
        manager.createNotificationChannel(channel)
    }
}
