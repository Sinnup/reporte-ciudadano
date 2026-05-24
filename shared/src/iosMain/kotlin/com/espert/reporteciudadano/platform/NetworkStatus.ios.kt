package com.espert.reporteciudadano.platform

// On iOS, the app always attempts reverse geocoding; CLGeocoder will fail gracefully
// if there is no network, and the LocationDisplay.Coordinates fallback will be shown.
// A proper NWPathMonitor implementation requires an async callback which does not fit
// the synchronous `expect fun` contract at this time.
actual fun isNetworkAvailable(): Boolean = true
