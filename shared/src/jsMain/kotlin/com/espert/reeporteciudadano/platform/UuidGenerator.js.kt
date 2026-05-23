package com.espert.reeporteciudadano.platform

actual fun generateUuid(): String {
    val hex = (0..31).map { (0..15).random().toString(16) }.joinToString("")
    return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-4${hex.substring(13, 16)}-${hex.substring(16, 20)}-${hex.substring(20, 32)}"
}
