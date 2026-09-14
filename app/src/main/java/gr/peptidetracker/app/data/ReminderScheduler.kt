package gr.peptidetracker.app.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import gr.peptidetracker.app.MainActivity
import gr.peptidetracker.app.R
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {
    private const val WORK_PREFIX = "peptide-reminder-"
    private const val SNOOZE_PREFIX = "peptide-reminder-snooze-"
    private const val SNOOZE_TAG = "peptide-reminder-snooze-all"

    fun schedule(context: Context, reminder: ReminderEntry) {
        if (!reminder.enabled) {
            cancel(context, reminder.id)
            return
        }

        val delayMs = max(0L, reminder.scheduledAt - System.currentTimeMillis())
        val data = Data.Builder()
            .putLong(ReminderWorker.KEY_REMINDER_ID, reminder.id)
            .putLong(ReminderWorker.KEY_EXPECTED_AT, reminder.scheduledAt)
            .putBoolean(ReminderWorker.KEY_IS_SNOOZE, false)
            .build()

        val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(WORK_PREFIX + reminder.id)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WORK_PREFIX + reminder.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, reminderId: Long) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(WORK_PREFIX + reminderId)
    }

    fun rescheduleAll(context: Context, reminders: List<ReminderEntry>) {
        reminders.forEach { reminder ->
            if (reminder.enabled) schedule(context, reminder) else cancel(context, reminder.id)
        }
    }

    fun cancelAllSnoozes(context: Context) {
        WorkManager.getInstance(context.applicationContext)
            .cancelAllWorkByTag(SNOOZE_TAG)
    }

    fun snooze(
        context: Context,
        reminderId: Long,
        peptide: String,
        note: String,
        minutes: Long = 15L
    ) {
        val runAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)
        val data = Data.Builder()
            .putLong(ReminderWorker.KEY_REMINDER_ID, reminderId)
            .putBoolean(ReminderWorker.KEY_IS_SNOOZE, true)
            .putString(ReminderWorker.KEY_PEPTIDE, peptide)
            .putString(ReminderWorker.KEY_NOTE, note)
            .build()

        val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag(SNOOZE_PREFIX + reminderId)
            .addTag(SNOOZE_TAG)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            SNOOZE_PREFIX + reminderId + "-" + runAt,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        val isSnooze = inputData.getBoolean(KEY_IS_SNOOZE, false)

        if (isSnooze) {
            val peptide = inputData.getString(KEY_PEPTIDE).orEmpty()
            val note = inputData.getString(KEY_NOTE).orEmpty()
            if (peptide.isNotBlank()) {
                ReminderNotifications.show(applicationContext, reminderId, peptide, note)
            }
            return Result.success()
        }

        if (reminderId <= 0L) return Result.success()

        val store = LocalStore(applicationContext)
        val reminder = store.reminders().firstOrNull { it.id == reminderId }
            ?: return Result.success()

        if (!reminder.enabled) return Result.success()

        val expectedAt = inputData.getLong(KEY_EXPECTED_AT, -1L)
        if (expectedAt > 0L && expectedAt != reminder.scheduledAt) {
            return Result.success()
        }

        ReminderNotifications.show(
            context = applicationContext,
            reminderId = reminder.id,
            peptide = reminder.peptide,
            note = reminder.note
        )

        val next = store.advanceReminderAfterFire(reminder.id)
        if (next != null && next.enabled) {
            ReminderScheduler.schedule(applicationContext, next)
        }

        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_EXPECTED_AT = "expected_at"
        const val KEY_IS_SNOOZE = "is_snooze"
        const val KEY_PEPTIDE = "peptide"
        const val KEY_NOTE = "note"
    }
}

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE) return

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val peptide = intent.getStringExtra(EXTRA_PEPTIDE).orEmpty()
        val note = intent.getStringExtra(EXTRA_NOTE).orEmpty()
        if (reminderId <= 0L || peptide.isBlank()) return

        ReminderScheduler.snooze(
            context = context,
            reminderId = reminderId,
            peptide = peptide,
            note = note,
            minutes = 15L
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(ReminderNotifications.notificationId(reminderId))
    }

    companion object {
        const val ACTION_SNOOZE = "gr.peptidetracker.app.action.SNOOZE_REMINDER"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_PEPTIDE = "peptide"
        const val EXTRA_NOTE = "note"
    }
}

private object ReminderNotifications {
    private const val CHANNEL_ID = "peptide_tracker_reminders"
    private const val CHANNEL_NAME = "Υπενθυμίσεις"
    private const val CHANNEL_DESCRIPTION = "Προσωπικές υπενθυμίσεις που έχει ορίσει ο χρήστης"

    fun show(context: Context, reminderId: Long, peptide: String, note: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId(reminderId),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE
            putExtra(ReminderActionReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderActionReceiver.EXTRA_PEPTIDE, peptide)
            putExtra(ReminderActionReceiver.EXTRA_NOTE, note)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId(reminderId) + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (note.isBlank()) {
            peptide
        } else {
            peptide + " · " + note
        }

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Peptide Tracker · Υπενθύμιση")
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_launcher_monochrome),
                    "Σε 15′",
                    snoozePendingIntent
                ).build()
            )
            .build()

        manager.notify(notificationId(reminderId), notification)
    }

    fun notificationId(reminderId: Long): Int {
        val folded = reminderId xor (reminderId ushr 32)
        return (folded and 0x7FFFFFFF).toInt()
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
