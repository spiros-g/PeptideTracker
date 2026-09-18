// Copyright © 2026 Kagon Digital Media & Commerce.
// All rights reserved. See LICENSE for permitted use.

package gr.peptidetracker.app.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import gr.peptidetracker.app.BuildConfig
import java.security.MessageDigest

object AppIntegrity {
    private val SHA256_HEX = Regex("[0-9a-f]{64}")

    fun isTrustedInstallation(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true

        val expected = BuildConfig.OFFICIAL_SIGNING_CERT_SHA256
            .trim()
            .lowercase()

        if (!SHA256_HEX.matches(expected)) return false

        val info = installedPackageInfo(
            packageManager = context.packageManager,
            packageName = context.packageName
        )

        return matchesExpectedSigner(
            expected = expected,
            actualSigners = signerDigests(info)
        )
    }

    internal fun matchesExpectedSigner(
        expected: String,
        actualSigners: Set<String>
    ): Boolean {
        val normalizedExpected = expected.trim().lowercase()
        if (!SHA256_HEX.matches(normalizedExpected)) return false

        return actualSigners.any { signer ->
            signer.trim().lowercase() == normalizedExpected
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
            .map { signature ->
                MessageDigest.getInstance("SHA-256")
                    .digest(signature.toByteArray())
                    .toHex()
            }
            .toSet()
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }
}
