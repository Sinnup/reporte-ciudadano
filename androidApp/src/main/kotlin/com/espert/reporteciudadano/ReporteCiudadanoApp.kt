package com.espert.reporteciudadano

import android.app.Application
import com.espert.reporteciudadano.di.appModule
import com.espert.reporteciudadano.platform.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ReporteCiudadanoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ReporteCiudadanoApp)
            modules(appModule(DatabaseDriverFactory(this@ReporteCiudadanoApp)))
        }
    }
}
