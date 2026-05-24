package com.espert.reeporteciudadano.platform

import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun isLocationEnabled(): Boolean = CLLocationManager.locationServicesEnabled()

actual fun openLocationSettings() {
    val url = NSURL.URLWithString("App-Prefs:root=LOCATION_SERVICES") ?: return
    UIApplication.sharedApplication.openURL(url)
}
