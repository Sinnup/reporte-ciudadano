@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

import android.content.Context
import androidx.work.*
import com.espert.reporteciudadano.cloudsync.worker.CloudSyncWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

private const val SYNC_WORK_TAG = "cloud_sync"
private const val PERIODIC_SYNC_WORK_NAME = "cloud_sync_periodic"
private const val EAGER_SYNC_WORK_NAME = "cloud_sync_eager"

actual object SyncScheduler : KoinComponent {
    private val context: Context by inject()

    actual fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<CloudSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SYNC_WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    actual fun cancelBackgroundSync() {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(EAGER_SYNC_WORK_NAME)
    }

    actual fun scheduleEagerSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<CloudSyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_WORK_TAG)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            EAGER_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
