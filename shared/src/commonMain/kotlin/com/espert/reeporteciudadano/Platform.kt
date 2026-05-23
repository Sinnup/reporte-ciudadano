package com.espert.reeporteciudadano

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform