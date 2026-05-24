package com.espert.reeporteciudadano.platform

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object LocationStatusHelper : KoinComponent {
    val context: Context by inject()
}

actual fun isLocationEnabled(): Boolean {
    val lm = LocationStatusHelper.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
           lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

actual fun openLocationSettings() {
    val ctx = LocationStatusHelper.context
    ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
