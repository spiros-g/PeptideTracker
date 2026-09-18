// Copyright © 2026 Kagon Digital Media & Commerce.
// All rights reserved. See LICENSE for permitted use.

package gr.peptidetracker.app

import gr.peptidetracker.app.i18n.t

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import gr.peptidetracker.app.data.AppIntegrity
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.UpdateNotificationWorker
import gr.peptidetracker.app.ui.AppTheme
import gr.peptidetracker.app.ui.PeptideTrackerApp
import gr.peptidetracker.app.ui.PremiumBackground
import gr.peptidetracker.app.ui.screens.AppLockScreen
import gr.peptidetracker.app.ui.screens.UnofficialBuildScreen

class MainActivity : FragmentActivity() {
    private lateinit var store: LocalStore
    private var unlocked by mutableStateOf(true)
    private var promptVisible = false
    private var backgroundedAt = 0L
    private var openUpdatesRequested by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        store = LocalStore(applicationContext)

        if (!AppIntegrity.isTrustedInstallation(applicationContext)) {
            setContent {
                AppTheme(themeMode = store.appTheme()) {
                    PremiumBackground {
                        UnofficialBuildScreen()
                    }
                }
            }
            return
        }

        unlocked = !store.appLockEnabled()
        openUpdatesRequested = intent?.getBooleanExtra(EXTRA_OPEN_UPDATES, false) == true
        UpdateNotificationWorker.schedule(applicationContext)

        setContent {
            if (unlocked) {
                PeptideTrackerApp(
                    store = store,
                    openUpdatesOnLaunch = openUpdatesRequested,
                    onUpdatesOpened = { openUpdatesRequested = false }
                )
            } else {
                AppTheme(themeMode = store.appTheme()) {
                    PremiumBackground {
                        AppLockScreen(onUnlock = ::requestUnlock)
                    }
                }
            }
        }

        if (!unlocked) requestUnlock()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_UPDATES, false)) {
            openUpdatesRequested = true
        }
    }

    override fun onStart() {
        super.onStart()
        if (
            ::store.isInitialized &&
            store.appLockEnabled() &&
            backgroundedAt > 0L &&
            SystemClock.elapsedRealtime() - backgroundedAt >= LOCK_AFTER_MS
        ) {
            unlocked = false
            requestUnlock()
        }
    }

    override fun onStop() {
        if (
            ::store.isInitialized &&
            store.appLockEnabled() &&
            !isChangingConfigurations
        ) {
            backgroundedAt = SystemClock.elapsedRealtime()
        }
        super.onStop()
    }

    private fun requestUnlock() {
        if (!::store.isInitialized || promptVisible || !store.appLockEnabled()) {
            unlocked = true
            return
        }

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val status = BiometricManager.from(this).canAuthenticate(authenticators)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            // Never trap the user outside the app if the device credential/biometric setup changed.
            store.setAppLockEnabled(false)
            unlocked = true
            return
        }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    promptVisible = false
                    backgroundedAt = 0L
                    unlocked = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    promptVisible = false
                }

                override fun onAuthenticationFailed() {
                    // Keep the app locked and let the system prompt accept another attempt.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(t("Ξεκλείδωμα Peptide Tracker"))
            .setSubtitle(t("Χρησιμοποίησε βιομετρικά ή το κλείδωμα της συσκευής"))
            .setAllowedAuthenticators(authenticators)
            .build()

        promptVisible = true
        prompt.authenticate(promptInfo)
    }

    companion object {
        const val EXTRA_OPEN_UPDATES = "open_updates"
        private const val LOCK_AFTER_MS = 30_000L
    }
}
