package gr.peptidetracker.app.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Straighten
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.BuildConfig
import gr.peptidetracker.app.data.LocalStore
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
    var customPeptides by remember { mutableStateOf(store.customPeptides()) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Ρυθμίσεις",
                subtitle = "Ασφάλεια, ιδιωτικότητα, προεπιλογές και δεδομένα εφαρμογής.",
                onBack = onBack
            )
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
                                "Κλείδωμα εφαρμογής",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (lockSupported) {
                                    "Ξεκλείδωμα με βιομετρικά ή PIN/κλείδωμα συσκευής. Ενεργοποιείται ξανά μετά από 30 δευτερόλεπτα στο background."
                                } else {
                                    "Δεν είναι διαθέσιμο συμβατό βιομετρικό ή κλείδωμα συσκευής."
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
                            "Ιδιωτικότητα ειδοποιήσεων",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (notificationDetails) {
                                "Οι ειδοποιήσεις μπορούν να εμφανίζουν όνομα πεπτιδίου και σημείωση."
                            } else {
                                "Στην οθόνη κλειδώματος εμφανίζεται μόνο γενική υπενθύμιση."
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
                                "Προεπιλεγμένη σύριγγα",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Χρησιμοποιείται ως αρχική επιλογή στον υπολογιστή.",
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
                                "Προσαρμοσμένα πεπτίδια",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Πρόσθεσε όνομα για ημερολόγιο, απόθεμα και υπενθυμίσεις. Δεν δημιουργείται επιστημονικό προφίλ ή οδηγία.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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
                                    store.deleteCustomPeptide(name)
                                    customPeptides = store.customPeptides()
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Διαγραφή " + name)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            customName = ""
                            showCustomDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Προσθήκη προσαρμοσμένου πεπτιδίου")
                    }
                }
            }
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
                                "Αντίγραφα ασφαλείας & εξαγωγή",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Απλό ή κρυπτογραφημένο backup, επαναφορά με προεπισκόπηση και εξαγωγή ημερολογίου σε CSV.",
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
                        Text("Άνοιγμα εργαλείων δεδομένων")
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
                                "Εισαγωγικές οθόνες",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Μπορείς να ξαναδείς την εισαγωγή χωρίς να διαγραφούν δεδομένα.",
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
                        Text("Εμφάνιση εισαγωγής ξανά")
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            "Σχετικά & ιδιωτικότητα",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        "Peptide Tracker GR v" + BuildConfig.VERSION_NAME,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Οι καταγραφές, το απόθεμα, οι μετρήσεις και οι υπενθυμίσεις αποθηκεύονται στη συσκευή. Το Android cloud backup είναι απενεργοποιημένο. Για ευαίσθητα exports προτίμησε το κρυπτογραφημένο backup.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = onOpenPrivacy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ιδιωτικότητα & ασφάλεια")
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = GlassSurfaceStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text("Νέο προσαρμοσμένο πεπτίδιο") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Το όνομα θα είναι διαθέσιμο μόνο για tracking. Δεν προστίθενται claims, δοσολογίες ή επιστημονικές πληροφορίες.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Όνομα") },
                        singleLine = true,
                        colors = premiumTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (store.addCustomPeptide(customName)) {
                            customPeptides = store.customPeptides()
                            showCustomDialog = false
                        }
                    },
                    enabled = customName.trim().length >= 2,
                    colors = premiumButtonColors()
                ) {
                    Text("Προσθήκη")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomDialog = false },
                    colors = premiumTextButtonColors()
                ) {
                    Text("Άκυρο")
                }
            }
        )
    }
}
