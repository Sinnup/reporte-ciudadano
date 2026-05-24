package com.espert.reporteciudadano.platform

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString()
