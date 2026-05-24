package com.espert.reporteciudadano

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform