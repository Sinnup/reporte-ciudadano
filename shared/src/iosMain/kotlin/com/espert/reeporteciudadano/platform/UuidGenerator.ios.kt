package com.espert.reeporteciudadano.platform

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString()
