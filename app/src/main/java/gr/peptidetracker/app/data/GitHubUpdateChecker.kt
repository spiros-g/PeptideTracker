package gr.peptidetracker.app.data

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val version: String,
    val releaseName: String,
    val releaseNotes: String,
    val releaseUrl: String,
    val apkUrl: String?,
    val apkSha256: String?,
    val apkSizeBytes: Long?
)

object GitHubUpdateChecker {
    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/spiros-g/PeptideTracker/releases/latest"
    private const val PLAY_STORE_INSTALLER = "com.android.vending"

    suspend fun check(currentVersion: String): AppUpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 8_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "PeptideTracker-Android")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }

            try {
                if (connection.responseCode !in 200..299) return@runCatching null
                val raw = connection.inputStream.bufferedReader().use { it.readText() }
                val release = JSONObject(raw)
                if (release.optBoolean("draft", false) || release.optBoolean("prerelease", false)) {
                    return@runCatching null
                }

                val tag = release.optString("tag_name").trim()
                if (tag.isBlank() || !isNewerVersion(tag, currentVersion)) {
                    return@runCatching null
                }

                val assets = release.optJSONArray("assets")
                var apkUrl: String? = null
                var apkSha256: String? = null
                var apkSizeBytes: Long? = null
                if (assets != null) {
                    for (index in 0 until assets.length()) {
                        val asset = assets.optJSONObject(index) ?: continue
                        val name = asset.optString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                            apkSha256 = normalizeSha256(asset.optString("digest"))
                            apkSizeBytes = asset.optLong("size").takeIf { it > 0L }
                            if (apkUrl != null) break
                        }
                    }
                }

                AppUpdateInfo(
                    version = tag.removePrefix("v").removePrefix("V"),
                    releaseName = release.optString("name").ifBlank { tag },
                    releaseNotes = release.optString("body").trim().take(1_500),
                    releaseUrl = release.optString("html_url"),
                    apkUrl = apkUrl,
                    apkSha256 = apkSha256,
                    apkSizeBytes = apkSizeBytes
                )
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    fun shouldUseGitHubUpdates(context: Context): Boolean {
        val installer = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        }.getOrNull()

        return installer != PLAY_STORE_INSTALLER
    }

    internal fun isNewerVersion(candidate: String, current: String): Boolean {
        val candidateParts = numericVersion(candidate)
        val currentParts = numericVersion(current)
        val length = maxOf(candidateParts.size, currentParts.size)
        for (index in 0 until length) {
            val next = candidateParts.getOrElse(index) { 0 }
            val installed = currentParts.getOrElse(index) { 0 }
            if (next != installed) return next > installed
        }
        return false
    }

    internal fun normalizeSha256(value: String): String? {
        val normalized = value.trim().removePrefix("sha256:").lowercase()
        return normalized.takeIf {
            it.length == 64 && it.all { char -> char in '0'..'9' || char in 'a'..'f' }
        }
    }

    private fun numericVersion(value: String): List<Int> =
        value.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .split(".")
            .map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
}
