package gr.peptidetracker.app.domain

import java.time.Instant
import java.time.ZoneId

object ReminderCadence {
    fun nextScheduledAt(
        scheduledAt: Long,
        repeatDays: Int,
        now: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (repeatDays <= 0) return null

        var next = Instant.ofEpochMilli(scheduledAt)
            .atZone(zoneId)
            .plusDays(repeatDays.toLong())

        val nowInstant = Instant.ofEpochMilli(now)
        while (!next.toInstant().isAfter(nowInstant)) {
            next = next.plusDays(repeatDays.toLong())
        }

        return next.toInstant().toEpochMilli()
    }
}
