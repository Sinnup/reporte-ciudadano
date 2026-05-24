package com.espert.reporteciudadano.domain.model

data class GeoLocation(val latitude: Double, val longitude: Double)

fun GeoLocation.formatCoordinates(): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lonDir = if (longitude >= 0) "E" else "W"
    val absLat = kotlin.math.abs(latitude)
    val absLon = kotlin.math.abs(longitude)
    return "${absLat.toFixed(4)}° $latDir, ${absLon.toFixed(4)}° $lonDir"
}

private fun Double.toFixed(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = kotlin.math.round(this * factor) / factor
    val intPart = rounded.toLong()
    val fracPart = kotlin.math.round((rounded - intPart) * factor).toLong()
    val fracStr = fracPart.toString().padStart(decimals, '0')
    return "$intPart.$fracStr"
}

private fun Double.pow(exp: Int): Double {
    var result = 1.0
    repeat(exp) { result *= this }
    return result
}
