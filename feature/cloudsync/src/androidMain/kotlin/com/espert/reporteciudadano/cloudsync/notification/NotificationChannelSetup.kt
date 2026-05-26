package com.espert.reporteciudadano.cloudsync.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.espert.reporteciudadano.cloudsync.R

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
            context.getString(R.string.sync_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.sync_notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }
}
