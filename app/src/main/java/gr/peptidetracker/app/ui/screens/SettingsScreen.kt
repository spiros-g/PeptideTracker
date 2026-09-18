package gr.peptidetracker.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import gr.peptidetracker.app.i18n.t

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.BuildConfig
import gr.peptidetracker.app.data.AppUpdateCheckResult
import gr.peptidetracker.app.data.AppUpdateInfo
import gr.peptidetracker.app.data.GitHubUpdateChecker
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.UpdateInstaller
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.GlassSurfaceStrong
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.TextPrimary
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumFilterChipColors
import gr.peptidetracker.app.ui.premiumTextButtonColors
import gr.peptidetracker.app.ui.premiumTextFieldColors
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    store: LocalStore,
    onBack: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenDataTools: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val lockSupported = remember {
        BiometricManager.from(context).canAuthenticate(authenticators) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    var defaultSyringe by remember { mutableIntStateOf(store.defaultSyringeUnitsPerMl()) }
    var appLock by remember { mutableStateOf(store.appLockEnabled()) }
    var notificationDetails by remember { mutableStateOf(store.notificationDetailsVisible()) }
    var appLanguage by remember { mutableStateOf(store.appLanguage()) }
    var appTheme by remember { mutableStateOf(store.appTheme()) }
    var customPeptides by remember { mutableStateOf(store.customPeptides()) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var pendingDeleteCustom by remember { mutableStateOf<String?>(null) }
    var customName by remember { mutableStateOf("") }
    var customError by remember { mutableStateOf("") }
    var externalLinkError by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val githubUpdatesEnabled = remember {
        GitHubUpdateChecker.shouldUseGitHubUpdates(context)
    }
    var checkingUpdate by remember { mutableStateOf(false) }
    var downloadingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateMessage by remember {
        mutableStateOf(
            if (githubUpdatesEnabled) {
                t("Έλεγχος για διαθέσιμη έκδοση...")
            } else {
                t("Οι ενημερώσεις αυτής της εγκατάστασης διαχειρίζονται από το Play Store.")
            }
        )
    }
    var pendingApk by remember { mutableStateOf<File?>(null) }

    val unknownSourcesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val apk = pendingApk
        if (apk != null && UpdateInstaller.canInstallPackages(context)) {
            runCatching { UpdateInstaller.launchInstaller(context, apk) }
                .onSuccess {
                    updateMessage = t("Ο Android installer άνοιξε. Επιβεβαίωσε την ενημέρωση.")
                    pendingApk = null
                }
                .onFailure {
                    updateMessage = t("Δεν ήταν δυνατό να ανοίξει ο installer.")
                }
        } else if (apk != null) {
            updateMessage = t("Χρειάζεται άδεια «Εγκατάσταση άγνωστων εφαρμογών» για το Peptide Tracker.")
        }
    }

    fun checkForUpdate() {
        if (!githubUpdatesEnabled || checkingUpdate || downloadingUpdate) return
        coroutineScope.launch {
            checkingUpdate = true
            updateMessage = t("Έλεγχος για ενημέρωση...")
            store.markUpdateCheck()
            when (val result = GitHubUpdateChecker.checkDetailed(BuildConfig.VERSION_NAME)) {
                is AppUpdateCheckResult.Available -> {
                    updateInfo = result.update
                    updateMessage = t("Διαθέσιμη έκδοση v") + result.update.version + "."
                }

                AppUpdateCheckResult.UpToDate -> {
                    updateInfo = null
                    updateMessage =
                        t("Έχεις την τελευταία έκδοση (v") + BuildConfig.VERSION_NAME + ")."
                }

                AppUpdateCheckResult.Failed -> {
                    updateInfo = null
                    updateMessage =
                        t("Η ενημέρωση δεν μπόρεσε να ελεγχθεί. Έλεγξε τη σύνδεσή σου και δοκίμασε ξανά.")
                }
            }
            checkingUpdate = false
        }
    }

    fun openExternal(url: String) {
        externalLinkError = ""
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            externalLinkError = t("Δεν ήταν δυνατό να ανοίξει ο σύνδεσμος.")
        }
    }

    fun installUpdate(update: AppUpdateInfo) {
        if (downloadingUpdate || checkingUpdate) return
        coroutineScope.launch {
            downloadingUpdate = true
            updateMessage = t("Λήψη και επαλήθευση v") + update.version + "..."
            runCatching {
                UpdateInstaller.downloadAndValidate(context, update)
            }.onSuccess { apk ->
                pendingApk = apk
                if (UpdateInstaller.canInstallPackages(context)) {
                    runCatching { UpdateInstaller.launchInstaller(context, apk) }
                        .onSuccess {
                            updateMessage = t("Ο Android installer άνοιξε. Επιβεβαίωσε την ενημέρωση.")
                            pendingApk = null
                        }
                        .onFailure {
                            updateMessage = t("Δεν ήταν δυνατό να ανοίξει ο Android installer.")
                        }
                } else {
                    updateMessage = t("Ενεργοποίησε «Να επιτρέπεται από αυτήν την πηγή» και γύρνα πίσω.")
                    unknownSourcesLauncher.launch(
                        UpdateInstaller.unknownSourcesIntent(context)
                    )
                }
            }.onFailure { error ->
                updateMessage = error.message
                    ?.takeIf { it.isNotBlank() }
                    ?: t("Η ενημέρωση απέτυχε.")
            }
            downloadingUpdate = false
        }
    }

    LaunchedEffect(Unit) {
        if (githubUpdatesEnabled) {
            checkForUpdate()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = t("Ρυθμίσεις"),
                subtitle = t("Ασφάλεια, ιδιωτικότητα, προεπιλογές και δεδομένα εφαρμογής."),
                onBack = onBack
            )
        }

        item {
            SettingsSectionTitle(t("Εμφάνιση & γλώσσα"))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Language,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Γλώσσα εφαρμογής"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Επίλεξε Ελληνικά, Αγγλικά ή ακολούθησε τη γλώσσα της συσκευής."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "system" to t("Σύστημα"),
                            "el" to t("Ελληνικά"),
                            "en" to t("Αγγλικά")
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = appLanguage == code,
                                onClick = {
                                    if (appLanguage != code) {
                                        appLanguage = code
                                        store.setAppLanguage(code)
                                        (context as? Activity)?.recreate()
                                    }
                                },
                                label = { Text(label) },
                                colors = premiumFilterChipColors()
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Θέμα εφαρμογής"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Ακολούθησε το σύστημα ή επίλεξε σταθερά φωτεινό ή σκοτεινό θέμα."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "system" to t("Σύστημα"),
                            "dark" to t("Σκοτεινό"),
                            "light" to t("Φωτεινό")
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = appTheme == code,
                                onClick = {
                                    if (appTheme != code) {
                                        appTheme = code
                                        store.setAppTheme(code)
                                        (context as? Activity)?.recreate()
                                    }
                                },
                                label = { Text(label) },
                                colors = premiumFilterChipColors()
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSectionTitle(t("Εφαρμογή"))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.SystemUpdateAlt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Ενημερώσεις εφαρμογής"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Εγκατεστημένη έκδοση v") + BuildConfig.VERSION_NAME,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        updateMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (githubUpdatesEnabled) {
                        val available = updateInfo
                        if (available != null) {
                            Button(
                                onClick = { installUpdate(available) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !checkingUpdate &&
                                    !downloadingUpdate &&
                                    available.apkUrl != null,
                                colors = premiumButtonColors()
                            ) {
                                Text(
                                    if (downloadingUpdate) {
                                        t("Λήψη ενημέρωσης...")
                                    } else {
                                        t("Ενημέρωση σε v") + available.version
                                    }
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = ::checkForUpdate,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !checkingUpdate && !downloadingUpdate
                            ) {
                                Text(
                                    if (checkingUpdate) {
                                        t("Έλεγχος...")
                                    } else {
                                        t("Έλεγχος για ενημέρωση")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsSectionTitle(t("Ιδιωτικότητα & ασφάλεια"))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Κλείδωμα εφαρμογής"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (lockSupported) {
                                    t("Ξεκλείδωμα με βιομετρικά ή PIN/κλείδωμα συσκευής. Ενεργοποιείται ξανά μετά από 30 δευτερόλεπτα στο background.")
                                } else {
                                    t("Δεν είναι διαθέσιμο συμβατό βιομετρικό ή κλείδωμα συσκευής.")
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = appLock && lockSupported,
                            onCheckedChange = {
                                appLock = it
                                store.setAppLockEnabled(it)
                            },
                            enabled = lockSupported
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            t("Ιδιωτικότητα ειδοποιήσεων"),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (notificationDetails) {
                                t("Οι ειδοποιήσεις μπορούν να εμφανίζουν όνομα πεπτιδίου και σημείωση.")
                            } else {
                                t("Στην οθόνη κλειδώματος εμφανίζεται μόνο γενική υπενθύμιση.")
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = notificationDetails,
                        onCheckedChange = {
                            notificationDetails = it
                            store.setNotificationDetailsVisible(it)
                        }
                    )
                }
            }
        }

        item {
            SettingsSectionTitle(t("Προτιμήσεις καταγραφής"))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Straighten,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                t("Προεπιλεγμένη σύριγγα"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Χρησιμοποιείται ως αρχική επιλογή στον υπολογιστή."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100, 40).forEach { unitsPerMl ->
                            FilterChip(
                                selected = defaultSyringe == unitsPerMl,
                                onClick = {
                                    defaultSyringe = unitsPerMl
                                    store.setDefaultSyringeUnitsPerMl(unitsPerMl)
                                },
                                label = { Text("U-" + unitsPerMl) },
                                modifier = Modifier.weight(1f),
                                colors = premiumFilterChipColors()
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Science,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Προσαρμοσμένα πεπτίδια"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Πρόσθεσε όνομα για ημερολόγιο, απόθεμα και υπενθυμίσεις. Δεν δημιουργείται επιστημονικό προφίλ ή οδηγία."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (customPeptides.isEmpty()) {
                        Text(
                            t("Δεν έχεις προσθέσει προσαρμοσμένα πεπτίδια."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    customPeptides.forEach { name ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                name,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    pendingDeleteCustom = name
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = t("Διαγραφή ") + name)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            customName = ""
                            customError = ""
                            showCustomDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("Προσθήκη προσαρμοσμένου πεπτιδίου"))
                    }
                }
            }
        }

        item {
            SettingsSectionTitle(t("Δεδομένα"))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Backup,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                t("Αντίγραφα ασφαλείας & εξαγωγή"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Απλό ή κρυπτογραφημένο backup, επαναφορά με προεπισκόπηση και εξαγωγή ημερολογίου σε CSV."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Button(
                        onClick = onOpenDataTools,
                        modifier = Modifier.fillMaxWidth(),
                        colors = premiumButtonColors()
                    ) {
                        Text(t("Άνοιγμα εργαλείων δεδομένων"))
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                t("Εισαγωγικές οθόνες"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                t("Μπορείς να ξαναδείς την εισαγωγή χωρίς να διαγραφούν δεδομένα."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            store.setOnboardingComplete(false)
                            onReplayOnboarding()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("Εμφάνιση εισαγωγής ξανά"))
                    }
                }
            }
        }

        item {
            SettingsSectionTitle(t("Σχετικά"))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Σχετικά με την εφαρμογή"),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Peptide Tracker v" + BuildConfig.VERSION_NAME,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.BusinessCenter,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Δημιουργήθηκε από Kagon Digital Media & Commerce"),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("Σχεδιασμός και ανάπτυξη του Peptide Tracker."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { openExternal("https://kagon.gr") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("kagon.gr")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Storefront,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                "PeptidiaStore.gr",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("Για προϊόντα πεπτιδίων και πληροφορίες αγοράς μπορείς να επισκεφθείς το PeptidiaStore.gr. Είναι εξωτερικός σύνδεσμος και το Peptide Tracker δεν μοιράζεται τα δεδομένα καταγραφής σου."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { openExternal("https://peptidiastore.gr") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("PeptidiaStore.gr")
                    }

                    if (externalLinkError.isNotBlank()) {
                        Text(
                            externalLinkError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        t("Οι καταγραφές, το απόθεμα, οι μετρήσεις και οι υπενθυμίσεις αποθηκεύονται στη συσκευή. Το Android cloud backup είναι απενεργοποιημένο. Για ευαίσθητα exports προτίμησε το κρυπτογραφημένο backup."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedButton(
                        onClick = onOpenPrivacy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("Ιδιωτικότητα & ασφάλεια"))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    pendingDeleteCustom?.let { name ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCustom = null },
            containerColor = GlassSurfaceStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text(t("Διαγραφή προσαρμοσμένου πεπτιδίου;")) },
            text = {
                Text(
                    t("Θα αφαιρεθεί μόνο από τις διαθέσιμες επιλογές. Οι υπάρχουσες καταγραφές, το απόθεμα και οι υπενθυμίσεις δεν θα διαγραφούν.")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.deleteCustomPeptide(name)
                        customPeptides = store.customPeptides()
                        pendingDeleteCustom = null
                    }
                ) {
                    Text(
                        t("Διαγραφή"),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteCustom = null },
                    colors = premiumTextButtonColors()
                ) {
                    Text(t("Άκυρο"))
                }
            }
        )
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = GlassSurfaceStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text(t("Νέο προσαρμοσμένο πεπτίδιο")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t("Το όνομα θα είναι διαθέσιμο μόνο για tracking. Δεν προστίθενται claims, δοσολογίες ή επιστημονικές πληροφορίες."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = {
                            customName = it
                            if (customError.isNotBlank()) customError = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t("Όνομα")) },
                        singleLine = true,
                        isError = customError.isNotBlank(),
                        supportingText = {
                            if (customError.isNotBlank()) {
                                Text(customError)
                            }
                        },
                        colors = premiumTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (store.addCustomPeptide(customName)) {
                            customPeptides = store.customPeptides()
                            customError = ""
                            showCustomDialog = false
                        } else {
                            customError = t("Το όνομα είναι άκυρο ή υπάρχει ήδη.")
                        }
                    },
                    enabled = customName.trim().length >= 2,
                    colors = premiumButtonColors()
                ) {
                    Text(t("Προσθήκη"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomDialog = false },
                    colors = premiumTextButtonColors()
                ) {
                    Text(t("Άκυρο"))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, end = 4.dp)
    )
}
