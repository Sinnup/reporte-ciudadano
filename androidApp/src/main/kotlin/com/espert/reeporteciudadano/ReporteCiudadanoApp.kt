package com.espert.reeporteciudadano

import android.app.Application
import com.espert.reeporteciudadano.di.appModule
import com.espert.reeporteciudadano.platform.DatabaseDriverFactory
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
