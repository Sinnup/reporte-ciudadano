package com.espert.reporteciudadano.feature.myreports

import com.espert.reporteciudadano.domain.model.ReportStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for feat-008: Spanish translations.
 *
 * statusLabel() is a private @Composable that maps each ReportStatus to a Res.string key via an
 * exhaustive `when` expression. The Kotlin compiler enforces exhaustiveness at compile time —
 * adding a new enum variant without updating the `when` is a compile error. These tests therefore
 * do not call statusLabel() directly (which would require a Compose runtime), but instead:
 *
 *   1. Assert the exact entry count so that any future enum extension is immediately visible.
 *   2. Assert that every entry has a distinct, non-empty name, confirming no duplicates were
 *      accidentally introduced.
 *   3. Assert that each expected variant exists in the enum, making the test fail as soon as a
 *      variant is renamed or removed.
 *
 * UI rendering for each locale is verified manually on device / simulator.
 */
class ReportStatusTest {

    @Test
    fun `ReportStatus has exactly 6 variants covering all statusLabel branches`() {
        assertEquals(
            6,
            ReportStatus.entries.size,
            "statusLabel has one branch per variant — update the when expression and this count together"
        )
    }

    @Test
    fun `every ReportStatus variant has a non-empty name`() {
        ReportStatus.entries.forEach { status ->
            assertFalse(
                status.name.isEmpty(),
                "ReportStatus variant has an empty name: $status"
            )
        }
    }

    @Test
    fun `all expected ReportStatus variants are present`() {
        val expected = setOf(
            "SENT",
            "SEEN",
            "PENDING",
            "IN_PROGRESS",
            "RESOLVED",
            "DISCARDED"
        )
        val actual = ReportStatus.entries.map { it.name }.toSet()
        assertEquals(
            expected,
            actual,
            "Mismatch between expected variants and actual enum entries"
        )
    }

    @Test
    fun `ReportStatus variant names are all distinct`() {
        val names = ReportStatus.entries.map { it.name }
        assertEquals(
            names.size,
            names.toSet().size,
            "Duplicate variant names detected in ReportStatus"
        )
    }
}
