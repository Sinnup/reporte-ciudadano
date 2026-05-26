@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.espert.reporteciudadano.cloudsync.R
import com.espert.reporteciudadano.cloudsync.notification.SYNC_FAILURES_CHANNEL_ID
import com.espert.reporteciudadano.cloudsync.receiver.EXTRA_REPORT_ID
import com.espert.reporteciudadano.cloudsync.receiver.SyncRetryReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object SyncFailureNotifier : KoinComponent {
    private val context: Context by inject()

    actual fun notifySyncFailure(reportId: String, reportTitle: String) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        val notificationId = reportId.hashCode()

        val retryIntent = Intent(context, SyncRetryReceiver::class.java).apply {
            putExtra(EXTRA_REPORT_ID, reportId)
        }
        val retryPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            retryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = context.getString(R.string.sync_notification_body, reportTitle)
        val notification = NotificationCompat.Builder(context, SYNC_FAILURES_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.sync_notification_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .addAction(
                android.R.drawable.ic_menu_rotate,
                context.getString(R.string.sync_notification_action_retry),
                retryPendingIntent
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(notificationId, notification)
    }

    actual fun cancelNotification(reportId: String) {
        context.getSystemService<NotificationManager>()?.cancel(reportId.hashCode())
    }
}
