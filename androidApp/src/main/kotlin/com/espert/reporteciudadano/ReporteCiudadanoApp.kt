package com.espert.reporteciudadano

import android.app.Application
import com.espert.reporteciudadano.cloudsync.di.cloudSyncModule
import com.espert.reporteciudadano.cloudsync.notification.NotificationChannelSetup
import com.espert.reporteciudadano.cloudsync.platform.SyncScheduler
import com.espert.reporteciudadano.di.appModule
import com.espert.reporteciudadano.feature.reportform.ReportFormViewModel
import com.espert.reporteciudadano.platform.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class ReporteCiudadanoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ReporteCiudadanoApp)
            allowOverride(true)
            modules(
                appModule(DatabaseDriverFactory(this@ReporteCiudadanoApp)),
                cloudSyncModule,
                module {
                    viewModel {
                        ReportFormViewModel(get(), get(), onReportSaved = { SyncScheduler.scheduleEagerSync() })
                    }
                }
            )
        }
        NotificationChannelSetup.createSyncFailuresChannel(this)
        SyncScheduler.scheduleBackgroundSync()
    }
}
