package gr.peptidetracker.app.data

import gr.peptidetracker.app.i18n.t

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import gr.peptidetracker.app.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateInstaller {
    private const val MAX_APK_BYTES = 250L * 1024L * 1024L

    suspend fun downloadAndValidate(
        context: Context,
        update: AppUpdateInfo
    ): File = withContext(Dispatchers.IO) {
        val apkUrl = update.apkUrl ?: error(t("Η έκδοση δεν περιέχει APK."))

        val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
        updateDir.listFiles()?.forEach { old ->
            if (old.isFile) old.delete()
        }

        val safeVersion = update.version.replace(Regex("[^0-9A-Za-z._-]"), "_")
        val tempFile = File(updateDir, "PeptideTracker-" + safeVersion + ".download.apk")
        val finalFile = File(updateDir, "PeptideTracker-" + safeVersion + ".apk")

        download(apkUrl, tempFile)

        update.apkSha256?.let { expected ->
            val actual = sha256(tempFile)
            check(actual.equals(expected, ignoreCase = true)) {
                t("Το checksum του APK δεν ταιριάζει με το GitHub release.")
            }
        }

        validatePackage(context, tempFile, update.version)

        check(tempFile.renameTo(finalFile)) {
            t("Δεν ήταν δυνατή η προετοιμασία του APK.")
        }

        finalFile
    }

    fun canInstallPackages(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    fun unknownSourcesIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:" + context.packageName)
        )

    fun launchInstaller(context: Context, apkFile: File) {
        check(apkFile.isFile && apkFile.length() > 0L) {
            t("Το αρχείο ενημέρωσης δεν είναι διαθέσιμο.")
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun download(url: String, destination: File) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 60_000
            setRequestProperty("User-Agent", "PeptideTracker-Android")
            setRequestProperty("Accept", "application/vnd.android.package-archive,application/octet-stream")
        }

        try {
            check(connection.responseCode in 200..299) {
                t("Η λήψη απέτυχε (HTTP ") + connection.responseCode + ")."
            }

            val announcedSize = connection.contentLengthLong
            if (announcedSize > MAX_APK_BYTES) {
                error(t("Το APK είναι μεγαλύτερο από το επιτρεπόμενο όριο."))
            }

            var total = 0L
            connection.inputStream.use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_APK_BYTES) {
                            error(t("Το APK είναι μεγαλύτερο από το επιτρεπόμενο όριο."))
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }

            check(total > 1_000_000L) {
                t("Το αρχείο ενημέρωσης είναι ασυνήθιστα μικρό.")
            }
        } catch (error: Throwable) {
            destination.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun validatePackage(
        context: Context,
        apkFile: File,
        expectedVersion: String
    ) {
        val packageManager = context.packageManager
        val archive = archivePackageInfo(packageManager, apkFile)
            ?: error(t("Το ληφθέν αρχείο δεν είναι έγκυρο Android APK."))
        val installed = installedPackageInfo(packageManager, context.packageName)

        check(archive.packageName == context.packageName) {
            t("Το APK ανήκει σε διαφορετική εφαρμογή.")
        }

        val archiveVersion = archive.versionName.orEmpty().trim()
        check(archiveVersion == expectedVersion) {
            t("Η έκδοση του APK (") + archiveVersion +
                t(") δεν ταιριάζει με την αναμενόμενη (") + expectedVersion + ")."
        }

        check(versionCodeOf(archive) > versionCodeOf(installed)) {
            t("Το APK δεν έχει νεότερο versionCode από την εγκατεστημένη εφαρμογή.")
        }

        val installedSigners = signerDigests(installed)
        val archiveSigners = signerDigests(archive)
        check(installedSigners.isNotEmpty() && archiveSigners.isNotEmpty()) {
            t("Δεν ήταν δυνατή η επαλήθευση της υπογραφής του APK.")
        }
        check(installedSigners.any { it in archiveSigners }) {
            t("Η υπογραφή του APK δεν ταιριάζει με την εγκατεστημένη εφαρμογή.")
        }

        check(
            GitHubUpdateChecker.isNewerVersion(
                archiveVersion,
                BuildConfig.VERSION_NAME
            )
        ) {
            t("Το APK δεν είναι νεότερο από την τρέχουσα έκδοση.")
        }
    }

    private fun archivePackageInfo(
        packageManager: PackageManager,
        file: File
    ): PackageInfo? {
        val flags = signingFlags()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.absolutePath, flags)
        }
    }

    private fun installedPackageInfo(
        packageManager: PackageManager,
        packageName: String
    ): PackageInfo {
        val flags = signingFlags()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, flags)
        }
    }

    private fun signingFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

    private fun versionCodeOf(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }

    private fun signerDigests(info: PackageInfo): Set<String> {
        val signatures: Array<Signature> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = info.signingInfo ?: return emptySet()
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                info.signatures ?: emptyArray()
            }

        return signatures
            .map { sha256(it.toByteArray()) }
            .toSet()
    }

    private fun sha256(file: File): String =
        file.inputStream().buffered().use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
            digest.digest().toHex()
        }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .toHex()

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }
}
