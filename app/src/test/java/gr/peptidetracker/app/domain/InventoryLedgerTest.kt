package gr.peptidetracker.app.domain

import gr.peptidetracker.app.data.InventoryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryLedgerTest {
    private fun active(remaining: Double = 10.0, quantity: Int = 2) = InventoryEntry(
        id = 1L,
        peptide = "Test",
        vialMg = 10.0,
        quantity = quantity,
        batch = "",
        remainingMg = remaining,
        active = true,
        openedAt = 100L
    )

    @Test
    fun partialConsumptionReducesRemainingOnly() {
        val next = InventoryLedger.consume(active(), 2.5)!!
        assertEquals(7.5, next.effectiveRemainingMg, 0.000001)
        assertEquals(2, next.quantity)
        assertTrue(next.active)
    }

    @Test
    fun exactConsumptionClosesVialWithoutOpeningNextOne() {
        val next = InventoryLedger.consume(active(remaining = 2.0), 2.0)!!
        assertEquals(1, next.quantity)
        assertEquals(0.0, next.effectiveRemainingMg, 0.000001)
        assertFalse(next.active)
        assertNull(next.openedAt)
    }

    @Test
    fun overConsumptionIsRejected() {
        assertNull(InventoryLedger.consume(active(remaining = 1.0), 1.5))
    }

    @Test
    fun restoringFinishedVialReactivatesOnlyRestoredAmount() {
        val finished = active(remaining = 1.0, quantity = 1)
        val consumed = InventoryLedger.consume(finished, 1.0)!!
        val restored = InventoryLedger.restore(consumed, 1.0, 999L)!!
        assertEquals(1, restored.quantity)
        assertEquals(1.0, restored.effectiveRemainingMg, 0.000001)
        assertTrue(restored.active)
        assertEquals(999L, restored.openedAt)
    }

    @Test
    fun restoringPartialConsumptionCapsAtVialCapacity() {
        val restored = InventoryLedger.restore(active(remaining = 9.0), 5.0, 999L)!!
        assertEquals(10.0, restored.effectiveRemainingMg, 0.000001)
    }
}
