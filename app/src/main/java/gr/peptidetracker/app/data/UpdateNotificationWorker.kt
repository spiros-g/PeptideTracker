package gr.peptidetracker.app.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import gr.peptidetracker.app.BuildConfig
import gr.peptidetracker.app.MainActivity
import gr.peptidetracker.app.R
import java.util.concurrent.TimeUnit

class UpdateNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!GitHubUpdateChecker.shouldUseGitHubUpdates(applicationContext)) {
            return Result.success()
        }

        val update = GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
            ?: return Result.success()

        if (!notificationsAllowed(applicationContext)) {
            return Result.success()
        }

        val preferences = applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        if (preferences.getString(KEY_LAST_NOTIFIED_VERSION, null) == update.version) {
            return Result.success()
        }

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Ενημερώσεις εφαρμογής",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ειδοποιήσεις για νέες εκδόσεις του Peptide Tracker"
            }
        )

        val openApp = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_OPEN_UPDATES, true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Νέα έκδοση Peptide Tracker")
            .setContentText("Η έκδοση " + update.version + " είναι διαθέσιμη για ενημέρωση.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
        preferences.edit()
            .putString(KEY_LAST_NOTIFIED_VERSION, update.version)
            .apply()

        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "peptide-tracker-update-check"
        private const val CHANNEL_ID = "app_updates"
        private const val NOTIFICATION_ID = 4702
        private const val PREFS_NAME = "update_notifications"
        private const val KEY_LAST_NOTIFIED_VERSION = "last_notified_version"

        fun schedule(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<UpdateNotificationWorker>(
                    24,
                    TimeUnit.HOURS
                )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun notificationsAllowed(context: Context): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
    }
}
