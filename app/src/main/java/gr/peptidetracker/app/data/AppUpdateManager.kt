package gr.peptidetracker.app.data

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import gr.peptidetracker.app.BuildConfig
import gr.peptidetracker.app.MainActivity
import gr.peptidetracker.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed interface UpdateDownloadResult {
    data class Success(val file: File) : UpdateDownloadResult
    data class Error(val message: String) : UpdateDownloadResult
}

object AppUpdateInstaller {
    private const val APK_MIME = "application/vnd.android.package-archive"
    private const val USER_AGENT = "PeptideTracker-Android-Updater"

    suspend fun downloadAndVerify(context: Context, update: AppUpdateInfo): UpdateDownloadResult =
        withContext(Dispatchers.IO) {
            val apkUrl = update.apkUrl
                ?: return@withContext UpdateDownloadResult.Error(
                    "Το GitHub release δεν περιέχει signed APK."
                )

            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val safeVersion = update.version.replace(Regex("[^0-9A-Za-z._-]"), "_")
            val finalFile = File(updateDir, "PeptideTracker-$safeVersion.apk")
            val partialFile = File(updateDir, "PeptideTracker-$safeVersion.apk.part")

            updateDir.listFiles()
                ?.filter { it != finalFile && it != partialFile }
                ?.forEach(File::delete)

            if (finalFile.isFile) {
                val expected = update.apkSha256
                if ((expected == null || sha256(finalFile) == expected) &&
                    verifyArchive(context, finalFile) == null
                ) {
                    return@withContext UpdateDownloadResult.Success(finalFile)
                }
                finalFile.delete()
            }

            partialFile.delete()
            val connection = runCatching {
                (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", USER_AGENT)
                }
            }.getOrElse {
                return@withContext UpdateDownloadResult.Error(
                    "Δεν ήταν δυνατή η σύνδεση για την ενημέρωση."
                )
            }

            try {
                if (connection.responseCode !in 200..299) {
                    return@withContext UpdateDownloadResult.Error(
                        "Η λήψη της ενημέρωσης απέτυχε (${connection.responseCode})."
                    )
                }

                var downloaded = 0L
                val digest = MessageDigest.getInstance("SHA-256")
                connection.inputStream.use { input ->
                    FileOutputStream(partialFile).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            if (count > 0) {
                                output.write(buffer, 0, count)
                                digest.update(buffer, 0, count)
                                downloaded += count
                            }
                        }
                        output.fd.sync()
                    }
                }

                if (downloaded <= 0L) {
                    partialFile.delete()
                    return@withContext UpdateDownloadResult.Error("Το APK που λήφθηκε είναι κενό.")
                }

                if (update.apkSizeBytes != null && downloaded != update.apkSizeBytes) {
                    partialFile.delete()
                    return@withContext UpdateDownloadResult.Error(
                        "Η λήψη δεν ολοκληρώθηκε σωστά. Δοκίμασε ξανά."
                    )
                }

                val actualSha = digest.digest().joinToString("") { "%02x".format(it) }
                if (update.apkSha256 != null && actualSha != update.apkSha256) {
                    partialFile.delete()
                    return@withContext UpdateDownloadResult.Error(
                        "Ο έλεγχος ακεραιότητας SHA-256 του APK απέτυχε."
                    )
                }

                if (finalFile.exists()) finalFile.delete()
                if (!partialFile.renameTo(finalFile)) {
                    partialFile.copyTo(finalFile, overwrite = true)
                    partialFile.delete()
                }

                val archiveError = verifyArchive(context, finalFile)
                if (archiveError != null) {
                    finalFile.delete()
                    return@withContext UpdateDownloadResult.Error(archiveError)
                }

                UpdateDownloadResult.Success(finalFile)
            } catch (_: Exception) {
                partialFile.delete()
                UpdateDownloadResult.Error(
                    "Παρουσιάστηκε σφάλμα κατά τη λήψη της ενημέρωσης."
                )
            } finally {
                connection.disconnect()
            }
        }

    fun canInstallPackages(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    fun unknownSourcesSettingsIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        )

    fun launchInstaller(context: Context, apkFile: File): Boolean {
        if (!apkFile.isFile) return false
        val uri = runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
        }.getOrNull() ?: return false

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIME)
            clipData = ClipData.newRawUri("Peptide Tracker update", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun verifyArchive(context: Context, apkFile: File): String? {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

        val archive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageArchiveInfo(
                apkFile.absolutePath,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
        } ?: return "Το αρχείο ενημέρωσης δεν είναι έγκυρο Android APK."

        if (archive.packageName != context.packageName) {
            return "Το APK ενημέρωσης ανήκει σε διαφορετική εφαρμογή."
        }

        val installed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, flags)
        }

        if (versionCode(archive) <= versionCode(installed)) {
            return "Το APK δεν είναι νεότερο από την εγκατεστημένη έκδοση."
        }

        val installedSigners = signerDigests(installed)
        val archiveSigners = signerDigests(archive)
        if (
            installedSigners.isEmpty() ||
            archiveSigners.isEmpty() ||
            installedSigners.intersect(archiveSigners).isEmpty()
        ) {
            return "Η ψηφιακή υπογραφή του APK δεν ταιριάζει με την εγκατεστημένη εφαρμογή."
        }
        return null
    }

    private fun versionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }

    private fun signerDigests(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signing = info.signingInfo ?: return emptySet()
            if (signing.hasMultipleSigners()) signing.apkContentsSigners
            else signing.signingCertificateHistory
        } else {
            @Suppress("DEPRECATION")
            info.signatures ?: return emptySet()
        }

        return signatures.mapTo(mutableSetOf()) { signature ->
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

object AppUpdateScheduler {
    private const val WORK_NAME = "peptide-tracker-update-check"

    fun ensureScheduled(context: Context) {
        val appContext = context.applicationContext
        val manager = WorkManager.getInstance(appContext)
        if (!GitHubUpdateChecker.shouldUseGitHubUpdates(appContext)) {
            manager.cancelUniqueWork(WORK_NAME)
            return
        }

        val request = PeriodicWorkRequest.Builder(
            AppUpdateCheckWorker::class.java,
            12,
            TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        manager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

class AppUpdateCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result {
        if (!GitHubUpdateChecker.shouldUseGitHubUpdates(applicationContext)) {
            return Result.success()
        }

        val update = runBlocking {
            GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
        } ?: return Result.success()

        val store = LocalStore(applicationContext)
        if (store.lastNotifiedUpdateVersion() == update.version) {
            return Result.success()
        }

        if (AppUpdateNotifications.show(applicationContext, update)) {
            store.markUpdateNotified(update.version)
        }
        return Result.success()
    }
}

private object AppUpdateNotifications {
    private const val CHANNEL_ID = "peptide_tracker_updates"
    private const val NOTIFICATION_ID = 4702

    fun show(context: Context, update: AppUpdateInfo): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Ενημερώσεις εφαρμογής",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Νέες εκδόσεις του Peptide Tracker"
                }
            )
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_UPDATE, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body =
            "Η έκδοση v${update.version} είναι διαθέσιμη. Πάτησε για ενημέρωση μέσα από την εφαρμογή."

        manager.notify(
            NOTIFICATION_ID,
            Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle("Peptide Tracker · Νέα έκδοση")
                .setContentText(body)
                .setStyle(Notification.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .addAction(
                    Notification.Action.Builder(null, "Ενημέρωση", pendingIntent).build()
                )
                .build()
        )
        return true
    }
}
