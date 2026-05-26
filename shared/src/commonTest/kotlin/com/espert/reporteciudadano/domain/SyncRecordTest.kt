package com.espert.reporteciudadano.domain

import com.espert.reporteciudadano.domain.model.SyncRecord
import com.espert.reporteciudadano.domain.model.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyncRecordTest {

    @Test
    fun `SyncRecord stores reportId correctly`() {
        val record = SyncRecord(
            reportId = "report-123",
            syncStatus = SyncStatus.PENDING,
            syncedAt = null,
            syncFailureCount = 0
        )

        assertEquals("report-123", record.reportId)
    }

    @Test
    fun `SyncRecord with null syncedAt represents a never-synced report`() {
        val record = SyncRecord(
            reportId = "report-abc",
            syncStatus = SyncStatus.PENDING,
            syncedAt = null,
            syncFailureCount = 0
        )

        assertNull(record.syncedAt)
        assertEquals(SyncStatus.PENDING, record.syncStatus)
    }

    @Test
    fun `SyncRecord with non-null syncedAt represents a successfully synced report`() {
        val epochMillis = 1_700_000_000_000L
        val record = SyncRecord(
            reportId = "report-xyz",
            syncStatus = SyncStatus.SYNCED,
            syncedAt = epochMillis,
            syncFailureCount = 0
        )

        assertEquals(epochMillis, record.syncedAt)
        assertEquals(SyncStatus.SYNCED, record.syncStatus)
        assertEquals(0, record.syncFailureCount)
    }

    @Test
    fun `SyncRecord with FAILED status and failure count stores count correctly`() {
        val record = SyncRecord(
            reportId = "report-fail",
            syncStatus = SyncStatus.FAILED,
            syncedAt = null,
            syncFailureCount = 3
        )

        assertEquals(SyncStatus.FAILED, record.syncStatus)
        assertEquals(3, record.syncFailureCount)
    }

    @Test
    fun `SyncRecord data class equality holds for identical values`() {
        val r1 = SyncRecord("id1", SyncStatus.PENDING, null, 0)
        val r2 = SyncRecord("id1", SyncStatus.PENDING, null, 0)

        assertEquals(r1, r2)
    }

    @Test
    fun `SyncRecord copy produces independent instance with updated fields`() {
        val original = SyncRecord("r1", SyncStatus.PENDING, null, 0)

        val updated = original.copy(syncStatus = SyncStatus.SYNCED, syncedAt = 1000L)

        assertEquals("r1", updated.reportId)
        assertEquals(SyncStatus.SYNCED, updated.syncStatus)
        assertEquals(1000L, updated.syncedAt)
        // original is unchanged
        assertEquals(SyncStatus.PENDING, original.syncStatus)
        assertNull(original.syncedAt)
    }
}
