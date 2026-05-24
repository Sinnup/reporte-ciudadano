package com.espert.reporteciudadano.domain

import com.espert.reporteciudadano.domain.model.LocationDisplay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LocationDisplayTest {

    @Test
    fun `LocationDisplay Loading is a singleton data object`() {
        val a = LocationDisplay.Loading
        val b = LocationDisplay.Loading

        assertSame(a, b)
        assertIs<LocationDisplay.Loading>(a)
    }

    @Test
    fun `LocationDisplay Loading is a LocationDisplay`() {
        val display: LocationDisplay = LocationDisplay.Loading

        assertIs<LocationDisplay>(display)
    }

    @Test
    fun `LocationDisplay Address holds text correctly`() {
        val display = LocationDisplay.Address("Av. Independencia 45, Centro, Guanajuato")

        assertEquals("Av. Independencia 45, Centro, Guanajuato", display.text)
    }

    @Test
    fun `LocationDisplay Address is a LocationDisplay`() {
        val display: LocationDisplay = LocationDisplay.Address("Some Address")

        assertIs<LocationDisplay.Address>(display)
    }

    @Test
    fun `LocationDisplay Address with empty text is valid`() {
        val display = LocationDisplay.Address("")

        assertEquals("", display.text)
    }

    @Test
    fun `LocationDisplay Coordinates holds latitude and longitude correctly`() {
        val display = LocationDisplay.Coordinates(latitude = 18.4861, longitude = -69.9312)

        assertEquals(18.4861, display.latitude)
        assertEquals(-69.9312, display.longitude)
    }

    @Test
    fun `LocationDisplay Coordinates is a LocationDisplay`() {
        val display: LocationDisplay = LocationDisplay.Coordinates(0.0, 0.0)

        assertIs<LocationDisplay.Coordinates>(display)
    }

    @Test
    fun `LocationDisplay Coordinates with zero values is valid`() {
        val display = LocationDisplay.Coordinates(latitude = 0.0, longitude = 0.0)

        assertEquals(0.0, display.latitude)
        assertEquals(0.0, display.longitude)
    }

    @Test
    fun `different LocationDisplay subtypes are not equal to each other`() {
        val loading = LocationDisplay.Loading
        val address = LocationDisplay.Address("text")
        val coordinates = LocationDisplay.Coordinates(1.0, 2.0)

        assertTrue(loading != address)
        assertTrue(loading != coordinates)
        assertTrue(address != coordinates)
    }

    @Test
    fun `LocationDisplay Address data class equality checks text`() {
        val a = LocationDisplay.Address("Same Address")
        val b = LocationDisplay.Address("Same Address")
        val c = LocationDisplay.Address("Different Address")

        assertEquals(a, b)
        assertTrue(a != c)
    }

    @Test
    fun `LocationDisplay Coordinates data class equality checks lat and lon`() {
        val a = LocationDisplay.Coordinates(18.4861, -69.9312)
        val b = LocationDisplay.Coordinates(18.4861, -69.9312)
        val c = LocationDisplay.Coordinates(0.0, 0.0)

        assertEquals(a, b)
        assertTrue(a != c)
    }
}
