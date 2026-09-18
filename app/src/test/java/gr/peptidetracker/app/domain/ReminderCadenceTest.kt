package gr.peptidetracker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderCadenceTest {
    @Test
    fun oneOffHasNoNextOccurrence() {
        assertNull(ReminderCadence.nextScheduledAt(1_000L, 0, 2_000L))
    }

    @Test
    fun dailyReminderAdvancesPastNow() {
        val day = 24L * 60L * 60L * 1000L
        val start = 1_000L
        assertEquals(
            start + 3L * day,
            ReminderCadence.nextScheduledAt(start, 1, start + 2L * day)
        )
    }

    @Test
    fun customThreeDayReminderAdvancesPastNow() {
        val day = 24L * 60L * 60L * 1000L
        val start = 1_000L
        assertEquals(
            start + 6L * day,
            ReminderCadence.nextScheduledAt(start, 3, start + 4L * day)
        )
    }

    @Test
    fun weeklyReminderKeepsSevenDayCadence() {
        val day = 24L * 60L * 60L * 1000L
        val start = 1_000L
        assertEquals(
            start + 14L * day,
            ReminderCadence.nextScheduledAt(start, 7, start + 10L * day)
        )
    }
}
