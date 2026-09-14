package gr.peptidetracker.app.domain

import gr.peptidetracker.app.data.InventoryEntry

object InventoryLedger {
    private const val EPSILON = 0.0000001

    fun consume(row: InventoryEntry, amountMg: Double): InventoryEntry? {
        if (amountMg <= 0 || !row.active || row.quantity <= 0) return null
        val remaining = row.effectiveRemainingMg
        if (amountMg > remaining + EPSILON) return null

        return if (amountMg < remaining - EPSILON) {
            row.copy(remainingMg = (remaining - amountMg).coerceAtLeast(0.0))
        } else {
            row.copy(
                quantity = (row.quantity - 1).coerceAtLeast(0),
                remainingMg = 0.0,
                active = false,
                openedAt = null
            )
        }
    }

    fun restore(row: InventoryEntry, amountMg: Double, now: Long): InventoryEntry? {
        if (amountMg <= 0) return null

        return when {
            row.active && row.quantity > 0 -> {
                row.copy(
                    remainingMg = (row.effectiveRemainingMg + amountMg)
                        .coerceAtMost(row.vialMg)
                )
            }

            row.effectiveRemainingMg <= EPSILON -> {
                row.copy(
                    quantity = row.quantity + 1,
                    remainingMg = amountMg.coerceAtMost(row.vialMg),
                    active = true,
                    openedAt = now
                )
            }

            else -> {
                row.copy(
                    remainingMg = (row.effectiveRemainingMg + amountMg)
                        .coerceAtMost(row.vialMg)
                )
            }
        }
    }
}
