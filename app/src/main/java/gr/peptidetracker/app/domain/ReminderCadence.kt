package gr.peptidetracker.app.domain

object ReminderCadence {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun nextScheduledAt(
        scheduledAt: Long,
        repeatDays: Int,
        now: Long
    ): Long? {
        if (repeatDays <= 0) return null
        val step = repeatDays.toLong() * DAY_MS
        var next = scheduledAt + step
        while (next <= now) next += step
        return next
    }
}
