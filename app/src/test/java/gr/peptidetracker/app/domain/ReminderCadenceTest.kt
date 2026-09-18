package gr.peptidetracker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

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
    fun recurrenceKeepsLocalClockTimeAcrossDstChange() {
        val zone = ZoneId.of("Europe/Athens")
        val start = LocalDateTime.of(2026, 10, 24, 20, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val next = ReminderCadence.nextScheduledAt(start, 1, now, zone)!!
        val local = java.time.Instant.ofEpochMilli(next).atZone(zone)

        assertEquals(20, local.hour)
        assertEquals(25, local.dayOfMonth)
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
