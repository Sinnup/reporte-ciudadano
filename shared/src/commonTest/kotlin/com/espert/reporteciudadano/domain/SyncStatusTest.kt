package com.espert.reporteciudadano.domain

import com.espert.reporteciudadano.domain.model.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SyncStatusTest {

    @Test
    fun `SyncStatus has exactly four variants`() {
        assertEquals(4, SyncStatus.entries.size)
    }

    @Test
    fun `all four expected SyncStatus variants exist`() {
        val expected = setOf("PENDING", "IN_PROGRESS", "SYNCED", "FAILED")
        val actual = SyncStatus.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `valueOf round-trips PENDING correctly`() {
        assertEquals(SyncStatus.PENDING, SyncStatus.valueOf("PENDING"))
    }

    @Test
    fun `valueOf round-trips IN_PROGRESS correctly`() {
        assertEquals(SyncStatus.IN_PROGRESS, SyncStatus.valueOf("IN_PROGRESS"))
    }

    @Test
    fun `valueOf round-trips SYNCED correctly`() {
        assertEquals(SyncStatus.SYNCED, SyncStatus.valueOf("SYNCED"))
    }

    @Test
    fun `valueOf round-trips FAILED correctly`() {
        assertEquals(SyncStatus.FAILED, SyncStatus.valueOf("FAILED"))
    }

    @Test
    fun `every SyncStatus entry valueOf produces a non-null value`() {
        SyncStatus.entries.forEach { status ->
            assertNotNull(SyncStatus.valueOf(status.name))
        }
    }

    @Test
    fun `SyncStatus entry names are all distinct`() {
        val names = SyncStatus.entries.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }
}
