package com.espert.reporteciudadano.domain

import com.espert.reporteciudadano.domain.model.GeoLocation
import com.espert.reporteciudadano.domain.model.formatCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoLocationTest {

    @Test
    fun `formatCoordinates returns N and E for positive lat and lon`() {
        val location = GeoLocation(latitude = 18.4861, longitude = 69.9312)

        val result = location.formatCoordinates()

        assertEquals("18.4861° N, 69.9312° E", result)
    }

    @Test
    fun `formatCoordinates returns N and W for positive lat and negative lon`() {
        val location = GeoLocation(latitude = 19.4326, longitude = -99.1332)

        val result = location.formatCoordinates()

        assertEquals("19.4326° N, 99.1332° W", result)
    }

    @Test
    fun `formatCoordinates returns S and W for negative lat and negative lon`() {
        val location = GeoLocation(latitude = -34.6037, longitude = -58.3816)

        val result = location.formatCoordinates()

        assertEquals("34.6037° S, 58.3816° W", result)
    }

    @Test
    fun `formatCoordinates returns S and E for negative lat and positive lon`() {
        val location = GeoLocation(latitude = -33.8688, longitude = 151.2093)

        val result = location.formatCoordinates()

        assertEquals("33.8688° S, 151.2093° E", result)
    }

    @Test
    fun `formatCoordinates handles equator and prime meridian (zero values)`() {
        val location = GeoLocation(latitude = 0.0, longitude = 0.0)

        val result = location.formatCoordinates()

        assertEquals("0.0000° N, 0.0000° E", result)
    }

    @Test
    fun `formatCoordinates formats to exactly four decimal places`() {
        val location = GeoLocation(latitude = 1.0, longitude = 2.0)

        val result = location.formatCoordinates()

        assertTrue(result.contains("1.0000°"), "Expected 4 decimal places for latitude, got: $result")
        assertTrue(result.contains("2.0000°"), "Expected 4 decimal places for longitude, got: $result")
    }

    @Test
    fun `formatCoordinates rounds to four decimal places`() {
        val location = GeoLocation(latitude = 18.48614999, longitude = -69.93124999)

        val result = location.formatCoordinates()

        assertTrue(result.startsWith("18.4861°"), "Expected rounding to 4 decimals, got: $result")
        assertTrue(result.contains("69.9312°"), "Expected rounding to 4 decimals, got: $result")
    }
}
