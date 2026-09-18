package gr.peptidetracker.app

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import gr.peptidetracker.app.data.AppUpdateScheduler
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.ui.AppTheme
import gr.peptidetracker.app.ui.PeptideTrackerApp
import gr.peptidetracker.app.ui.PremiumBackground
import gr.peptidetracker.app.ui.screens.AppLockScreen

class MainActivity : FragmentActivity() {
    private lateinit var store: LocalStore
    private var unlocked by mutableStateOf(true)
    private var updateRequestToken by mutableIntStateOf(0)
    private var promptVisible = false
    private var backgroundedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        store = LocalStore(applicationContext)
        unlocked = !store.appLockEnabled()
        if (intent.getBooleanExtra(EXTRA_OPEN_UPDATE, false)) updateRequestToken++

        AppUpdateScheduler.ensureScheduled(applicationContext)

        setContent {
            if (unlocked) {
                PeptideTrackerApp(store = store, updateRequestToken = updateRequestToken)
            } else {
                AppTheme {
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
        if (intent.getBooleanExtra(EXTRA_OPEN_UPDATE, false)) updateRequestToken++
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
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Ξεκλείδωμα Peptide Tracker GR")
            .setSubtitle("Χρησιμοποίησε βιομετρικά ή το κλείδωμα της συσκευής")
            .setAllowedAuthenticators(authenticators)
            .build()

        promptVisible = true
        prompt.authenticate(promptInfo)
    }

    companion object {
        const val EXTRA_OPEN_UPDATE = "gr.peptidetracker.app.extra.OPEN_UPDATE"
        private const val LOCK_AFTER_MS = 30_000L
    }
}
