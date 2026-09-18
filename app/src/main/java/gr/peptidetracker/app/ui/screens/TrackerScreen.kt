package gr.peptidetracker.app.ui.screens

import gr.peptidetracker.app.i18n.isGreekLanguage
import gr.peptidetracker.app.i18n.t

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.BackupCrypto
import gr.peptidetracker.app.data.BackupSummary
import gr.peptidetracker.app.data.InventoryEntry
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.ProgressEntry
import gr.peptidetracker.app.data.TrackerEntry
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.GlassSurfaceStrong
import gr.peptidetracker.app.ui.NeonRose
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage
import gr.peptidetracker.app.ui.TextPrimary
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumFilterChipColors
import gr.peptidetracker.app.ui.premiumTextButtonColors
import gr.peptidetracker.app.ui.premiumTextFieldColors
import gr.peptidetracker.app.ui.components.PeptidePickerDialog
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun TrackerScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    initialSection: Int = 0,
    newLogRequest: Int = 0,
    inventoryPeptidePreset: String? = null,
    dataToolsRequest: Int = 0,
    onConsumeNewLogRequest: () -> Unit = {},
    onConsumeInventoryPreset: () -> Unit = {},
    onConsumeDataToolsRequest: () -> Unit = {},
    onOpenCalculator: (InventoryEntry) -> Unit = {}
) {
    var section by remember { mutableIntStateOf(initialSection.coerceIn(0, 3)) }
    var logs by remember { mutableStateOf(store.entries()) }
    var inventory by remember { mutableStateOf(store.inventory()) }
    var progress by remember { mutableStateOf(store.progress()) }
    val peptideNames = store.peptideNames()

    var editingLog by remember { mutableStateOf<TrackerEntry?>(null) }
    var repeatLogTemplate by remember { mutableStateOf<TrackerEntry?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }
    var editingInventory by remember { mutableStateOf<InventoryEntry?>(null) }
    var inventoryPreset by remember { mutableStateOf<String?>(null) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var editingProgress by remember { mutableStateOf<ProgressEntry?>(null) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showDataTools by remember { mutableStateOf(false) }
    var dataMessage by remember { mutableStateOf("") }
    var pendingRestoreRaw by remember { mutableStateOf<String?>(null) }
    var pendingRestoreSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var pendingDeleteLog by remember { mutableStateOf<TrackerEntry?>(null) }
    var pendingDeleteInventory by remember { mutableStateOf<InventoryEntry?>(null) }
    var pendingDeleteProgress by remember { mutableStateOf<ProgressEntry?>(null) }
    var encryptedBackupPassword by remember { mutableStateOf("") }
    var showEncryptedBackupPassword by remember { mutableStateOf(false) }
    var pendingEncryptedRestore by remember { mutableStateOf<ByteArray?>(null) }
    var encryptedRestorePassword by remember { mutableStateOf("") }
    var showEncryptedRestorePassword by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun reloadAll() {
        logs = store.entries()
        inventory = store.inventory()
        progress = store.progress()
    }

    fun queueRestore(raw: String) {
        val summary = store.inspectBackup(raw)
        if (summary == null) {
            pendingRestoreRaw = null
            pendingRestoreSummary = null
            dataMessage = t("Το αρχείο backup δεν είναι έγκυρο.")
        } else {
            pendingRestoreRaw = raw
            pendingRestoreSummary = summary
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(store.exportJson())
                } ?: error(t("Δεν ήταν δυνατή η εγγραφή του αρχείου."))
            }.onSuccess {
                dataMessage = t("Το backup αποθηκεύτηκε.")
            }.onFailure {
                dataMessage = t("Αποτυχία αποθήκευσης backup.")
            }
        }
    }

    val encryptedBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val password = encryptedBackupPassword
        encryptedBackupPassword = ""
        if (uri != null && password.length >= 6) {
            scope.launch {
                val result = runCatching {
                    val bytes = withContext(Dispatchers.Default) {
                        BackupCrypto.encrypt(store.exportJson(), password.toCharArray())
                    }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(bytes)
                        } ?: error(t("Δεν ήταν δυνατή η εγγραφή του αρχείου."))
                    }
                }
                dataMessage = if (result.isSuccess) {
                    t("Το κρυπτογραφημένο backup αποθηκεύτηκε.")
                } else {
                    t("Αποτυχία κρυπτογραφημένου backup.")
                }
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(store.exportEntriesCsv())
                } ?: error(t("Δεν ήταν δυνατή η εγγραφή του αρχείου."))
            }.onSuccess {
                dataMessage = t("Το CSV αποθηκεύτηκε.")
            }.onFailure {
                dataMessage = t("Αποτυχία εξαγωγής CSV.")
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error(t("Κενό αρχείο."))
            }.onSuccess { bytes ->
                if (BackupCrypto.isEncrypted(bytes)) {
                    pendingEncryptedRestore = bytes
                    encryptedRestorePassword = ""
                    showEncryptedRestorePassword = true
                } else {
                    queueRestore(bytes.toString(Charsets.UTF_8))
                }
            }.onFailure {
                pendingRestoreRaw = null
                pendingRestoreSummary = null
                dataMessage = t("Το αρχείο backup δεν είναι έγκυρο.")
            }
        }
    }

    LaunchedEffect(initialSection) {
        section = initialSection.coerceIn(0, 3)
    }

    LaunchedEffect(newLogRequest) {
        if (newLogRequest > 0) {
            section = 0
            editingLog = null
            showLogDialog = true
            onConsumeNewLogRequest()
        }
    }

    LaunchedEffect(inventoryPeptidePreset) {
        if (!inventoryPeptidePreset.isNullOrBlank()) {
            section = 1
            inventoryPreset = inventoryPeptidePreset
            editingInventory = null
            showInventoryDialog = true
            onConsumeInventoryPreset()
        }
    }

    LaunchedEffect(dataToolsRequest) {
        if (dataToolsRequest > 0) {
            showDataTools = true
            onConsumeDataToolsRequest()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 18.dp)
        ) {
        Column(
            Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumTopBar(
                title = t("Ημερολόγιο"),
                subtitle = t("Χρήσεις, ενεργά φιαλίδια, απόθεμα και προσωπικές μετρήσεις.")
            )

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    TrackerTab(t("Χρήσεις"), section == 0, Modifier.weight(1f)) { section = 0 }
                    TrackerTab(t("Απόθεμα"), section == 1, Modifier.weight(1f)) { section = 1 }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    TrackerTab(t("Μετρήσεις"), section == 2, Modifier.weight(1f)) { section = 2 }
                    TrackerTab(t("Στατιστικά"), section == 3, Modifier.weight(1f)) { section = 3 }
                }
            }

            TextButton(
                onClick = { showDataTools = true },
                colors = premiumTextButtonColors()
            ) {
                Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("Αντίγραφα ασφαλείας & εξαγωγή"))
            }

            if (dataMessage.isNotBlank()) {
                Text(
                    dataMessage,
                    color = ElectricCyan,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        when (section) {
            0 -> LogsSection(
                rows = logs,
                imageIndex = imageIndex,
                onAdd = {
                    editingLog = null
                    repeatLogTemplate = null
                    showLogDialog = true
                },
                onEdit = {
                    editingLog = it
                    repeatLogTemplate = null
                    showLogDialog = true
                },
                onRepeat = {
                    editingLog = null
                    repeatLogTemplate = it
                    showLogDialog = true
                },
                onDelete = {
                    pendingDeleteLog = it
                }
            )

            1 -> InventorySection(
                rows = inventory,
                imageIndex = imageIndex,
                onAdd = {
                    inventoryPreset = null
                    editingInventory = null
                    showInventoryDialog = true
                },
                onActivate = {
                    store.activateInventory(it.id)
                    inventory = store.inventory()
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onEdit = {
                    editingInventory = it
                    inventoryPreset = null
                    showInventoryDialog = true
                },
                onDelete = { row ->
                    val hasLinkedUsage = logs.any { entry ->
                        entry.inventoryId == row.id && entry.inventoryAppliedMg != null
                    }
                    if (hasLinkedUsage) {
                        dataMessage = t("Δεν μπορεί να διαγραφεί απόθεμα που συνδέεται με καταγραφές. Διέγραψε ή αποσύνδεσε πρώτα τις σχετικές καταγραφές.")
                    } else {
                        pendingDeleteInventory = row
                    }
                },
                onCalculator = onOpenCalculator
            )

            2 -> ProgressSection(
                rows = progress,
                onAdd = {
                    editingProgress = null
                    showProgressDialog = true
                },
                onEdit = {
                    editingProgress = it
                    showProgressDialog = true
                },
                onDelete = {
                    pendingDeleteProgress = it
                }
            )

            else -> StatisticsSection(
                logs = logs,
                inventory = inventory,
                progress = progress
            )
        }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        )
    }

    if (showLogDialog) {
        LogEditorDialog(
            current = editingLog,
            template = repeatLogTemplate,
            inventory = inventory,
            peptideNames = peptideNames,
            onDismiss = { showLogDialog = false },
            onSave = { peptide, value, unit, note, site, createdAt, inventoryId, subtract ->
                val current = editingLog
                val saved = if (current == null) {
                    store.addEntry(
                        peptide = peptide,
                        amountValue = value,
                        unit = unit,
                        note = note,
                        site = site,
                        createdAt = createdAt,
                        inventoryId = inventoryId,
                        subtractFromInventory = subtract
                    )
                } else {
                    store.updateEntry(
                        id = current.id,
                        peptide = peptide,
                        amountValue = value,
                        unit = unit,
                        note = note,
                        site = site,
                        createdAt = createdAt
                    )
                }
                if (saved) {
                    reloadAll()
                    showLogDialog = false
                    repeatLogTemplate = null
                    dataMessage = t("Η καταγραφή αποθηκεύτηκε.")
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                    dataMessage = t("Δεν υπάρχει αρκετό υπόλοιπο στο συνδεδεμένο ενεργό vial για αυτή την καταγραφή.")
                }
            }
        )
    }

    if (showInventoryDialog) {
        InventoryEditorDialog(
            current = editingInventory,
            presetPeptide = inventoryPreset,
            peptideNames = peptideNames,
            onDismiss = { showInventoryDialog = false },
            onSave = { peptide, vial, quantity, batch, remaining, diluentMl, syringeUnitsPerMl, vendor, note, purchaseDate, expiryDate ->
                val current = editingInventory
                if (current == null) {
                    store.addInventory(
                        peptide = peptide,
                        vial = vial,
                        quantity = quantity,
                        batch = batch,
                        diluentMl = diluentMl,
                        syringeUnitsPerMl = syringeUnitsPerMl,
                        vendor = vendor,
                        note = note,
                        purchaseDate = purchaseDate,
                        expiryDate = expiryDate
                    )
                } else {
                    store.updateInventoryDetails(
                        id = current.id,
                        peptide = peptide,
                        vialMg = vial,
                        quantity = quantity,
                        batch = batch,
                        remainingMg = remaining,
                        diluentMl = diluentMl,
                        syringeUnitsPerMl = syringeUnitsPerMl,
                        vendor = vendor,
                        note = note,
                        purchaseDate = purchaseDate,
                        expiryDate = expiryDate
                    )
                }
                inventory = store.inventory()
                dataMessage = t("Το απόθεμα αποθηκεύτηκε.")
                showInventoryDialog = false
            }
        )
    }

    if (showProgressDialog) {
        ProgressEditorDialog(
            current = editingProgress,
            onDismiss = { showProgressDialog = false },
            onSave = { weight, waist, note, createdAt ->
                val current = editingProgress
                if (current == null) {
                    store.addProgress(weight, waist, note)
                } else {
                    store.updateProgress(current.id, weight, waist, note, createdAt)
                }
                progress = store.progress()
                dataMessage = t("Η μέτρηση αποθηκεύτηκε.")
                showProgressDialog = false
            }
        )
    }

    if (showDataTools) {
        AlertDialog(
            onDismissRequest = { showDataTools = false },
            containerColor = GlassSurfaceStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text(t("Δεδομένα εφαρμογής")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        t("Το backup περιλαμβάνει ημερολόγιο, απόθεμα, μετρήσεις, αγαπημένα και βασικές ρυθμίσεις. Όλα παραμένουν τοπικά εκτός αν εσύ αποθηκεύσεις ή μοιραστείς το αρχείο."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            showDataTools = false
                            backupLauncher.launch("PeptideTracker-backup.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = premiumButtonColors()
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text(t("Αποθήκευση backup"))
                    }
                    OutlinedButton(
                        onClick = {
                            showDataTools = false
                            encryptedBackupPassword = ""
                            showEncryptedBackupPassword = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("Κρυπτογραφημένο backup"))
                    }
                    OutlinedButton(
                        onClick = {
                            showDataTools = false
                            restoreLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text(t("Επαναφορά backup"))
                    }
                    OutlinedButton(
                        onClick = {
                            showDataTools = false
                            csvLauncher.launch("PeptideTracker-history.csv")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("Εξαγωγή ημερολογίου σε CSV"))
                    }
                    if (store.hasPreRestoreBackup()) {
                        OutlinedButton(
                            onClick = {
                                showDataTools = false
                                val ok = store.restorePreRestoreBackup()
                                if (ok) {
                                    reloadAll()
                                    dataMessage = t("Επαναφέρθηκε το snapshot πριν από το τελευταίο restore.")
                                } else {
                                    dataMessage = t("Δεν ήταν δυνατή η επαναφορά του προηγούμενου snapshot.")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(t("Επαναφορά πριν το τελευταίο restore"))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataTools = false }, colors = premiumTextButtonColors()) {
                    Text(t("Κλείσιμο"))
                }
            }
        )
    }

    if (showEncryptedBackupPassword) {
        PasswordDialog(
            title = t("Κρυπτογραφημένο backup"),
            text = t("Όρισε κωδικό τουλάχιστον 6 χαρακτήρων. Χωρίς αυτόν τον κωδικό το backup δεν μπορεί να επαναφερθεί."),
            password = encryptedBackupPassword,
            onPasswordChange = { encryptedBackupPassword = it },
            confirmText = t("Δημιουργία"),
            onConfirm = {
                showEncryptedBackupPassword = false
                encryptedBackupLauncher.launch("PeptideTracker-secure.ptbackup")
            },
            onDismiss = {
                encryptedBackupPassword = ""
                showEncryptedBackupPassword = false
            }
        )
    }

    if (showEncryptedRestorePassword) {
        PasswordDialog(
            title = t("Ξεκλείδωμα backup"),
            text = t("Πληκτρολόγησε τον κωδικό του κρυπτογραφημένου backup."),
            password = encryptedRestorePassword,
            onPasswordChange = { encryptedRestorePassword = it },
            confirmText = t("Ξεκλείδωμα"),
            onConfirm = {
                val bytes = pendingEncryptedRestore
                val password = encryptedRestorePassword
                encryptedRestorePassword = ""
                scope.launch {
                    val raw = if (bytes == null) {
                        null
                    } else {
                        withContext(Dispatchers.Default) {
                            BackupCrypto.decrypt(bytes, password.toCharArray())
                        }
                    }
                    if (raw == null) {
                        dataMessage = t("Λάθος κωδικός ή κατεστραμμένο κρυπτογραφημένο backup.")
                    } else {
                        queueRestore(raw)
                        pendingEncryptedRestore = null
                        showEncryptedRestorePassword = false
                    }
                }
            },
            onDismiss = {
                pendingEncryptedRestore = null
                encryptedRestorePassword = ""
                showEncryptedRestorePassword = false
            }
        )
    }

    pendingRestoreSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = {
                pendingRestoreRaw = null
                pendingRestoreSummary = null
            },
            containerColor = GlassSurfaceStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text(t("Επαναφορά backup")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t("Μπορείς να συγχωνεύσεις το backup με τα τρέχοντα δεδομένα ή να τα αντικαταστήσεις. Πριν από πλήρη αντικατάσταση αποθηκεύεται αυτόματα εσωτερικό snapshot ασφαλείας."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("Schema v" + summary.schema, fontWeight = FontWeight.Bold)
                    Text(t("Καταγραφές: ") + summary.entries)
                    Text(t("Απόθεμα: ") + summary.inventory)
                    Text(t("Μετρήσεις: ") + summary.progress)
                    Text(t("Υπενθυμίσεις: ") + summary.reminders)
                    Text(t("Αποθηκευμένοι υπολογισμοί: ") + summary.savedCalculations)
                    Text(t("Προσαρμοσμένα πεπτίδια: ") + summary.customPeptides)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val raw = pendingRestoreRaw
                            val ok = raw != null && store.mergeJson(raw)
                            if (ok) {
                                reloadAll()
                                dataMessage = t("Το backup συγχωνεύτηκε με τα τρέχοντα δεδομένα.")
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                dataMessage = t("Η συγχώνευση απέτυχε. Τα τρέχοντα δεδομένα διατηρήθηκαν.")
                            }
                            pendingRestoreRaw = null
                            pendingRestoreSummary = null
                        }
                    ) {
                        Text(t("Συγχώνευση"))
                    }
                    Button(
                        onClick = {
                            val raw = pendingRestoreRaw
                            val ok = raw != null && store.restoreJson(raw)
                            if (ok) {
                                reloadAll()
                                dataMessage = t("Το backup επαναφέρθηκε και οι ενεργές υπενθυμίσεις επαναπρογραμματίστηκαν.")
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                dataMessage = t("Η επαναφορά απέτυχε. Τα προηγούμενα δεδομένα διατηρήθηκαν.")
                            }
                            pendingRestoreRaw = null
                            pendingRestoreSummary = null
                        },
                        colors = premiumButtonColors()
                    ) {
                        Text(t("Αντικατάσταση"))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingRestoreRaw = null
                        pendingRestoreSummary = null
                    },
                    colors = premiumTextButtonColors()
                ) {
                    Text(t("Άκυρο"))
                }
            }
        )
    }

    pendingDeleteLog?.let { row ->
        ConfirmDeleteDialog(
            title = t("Διαγραφή καταγραφής;"),
            text = if (row.inventoryAppliedMg != null) {
                t("Η καταγραφή θα διαγραφεί και η ποσότητα που είχε αφαιρεθεί θα επιστραφεί στο συνδεδεμένο απόθεμα.")
            } else {
                t("Η καταγραφή θα διαγραφεί οριστικά.")
            },
            onConfirm = {
                val deleted = store.deleteEntry(row.id)
                reloadAll()
                pendingDeleteLog = null
                if (deleted != null) {
                    scope.launch {
                        if (
                            snackbarHostState.showSnackbar(
                                message = t("Η καταγραφή διαγράφηκε."),
                                actionLabel = t("ΑΝΑΙΡΕΣΗ"),
                                withDismissAction = true
                            ) == SnackbarResult.ActionPerformed
                        ) {
                            store.restoreDeletedEntry(deleted)
                            reloadAll()
                        }
                    }
                }
            },
            onDismiss = { pendingDeleteLog = null }
        )
    }

    pendingDeleteInventory?.let { row ->
        ConfirmDeleteDialog(
            title = t("Διαγραφή αποθέματος;"),
            text = row.peptide + t(" θα αφαιρεθεί από το απόθεμα."),
            onConfirm = {
                store.deleteInventory(row.id)
                reloadAll()
                pendingDeleteInventory = null
                scope.launch {
                    if (
                        snackbarHostState.showSnackbar(
                            message = t("Το απόθεμα διαγράφηκε."),
                            actionLabel = t("ΑΝΑΙΡΕΣΗ"),
                            withDismissAction = true
                        ) == SnackbarResult.ActionPerformed
                    ) {
                        store.restoreDeletedInventory(row)
                        reloadAll()
                    }
                }
            },
            onDismiss = { pendingDeleteInventory = null }
        )
    }

    pendingDeleteProgress?.let { row ->
        ConfirmDeleteDialog(
            title = t("Διαγραφή μέτρησης;"),
            text = t("Η μέτρηση ") + formatCompact(row.weight) + t(" kg θα διαγραφεί."),
            onConfirm = {
                store.deleteProgress(row.id)
                reloadAll()
                pendingDeleteProgress = null
                scope.launch {
                    if (
                        snackbarHostState.showSnackbar(
                            message = t("Η μέτρηση διαγράφηκε."),
                            actionLabel = t("ΑΝΑΙΡΕΣΗ"),
                            withDismissAction = true
                        ) == SnackbarResult.ActionPerformed
                    ) {
                        store.restoreDeletedProgress(row)
                        reloadAll()
                    }
                }
            },
            onDismiss = { pendingDeleteProgress = null }
        )
    }
}

@Composable
private fun PasswordDialog(
    title: String,
    text: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Κωδικός")) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = premiumTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = password.length >= 6,
                colors = premiumButtonColors()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text(t("Άκυρο"))
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(title) },
        text = { Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            Button(onClick = onConfirm, colors = premiumButtonColors()) {
                Text(t("Διαγραφή"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text(t("Άκυρο"))
            }
        }
    )
}

@Composable
private fun TrackerTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            )
        },
        modifier = modifier,
        colors = premiumFilterChipColors()
    )
}

@Composable
private fun LogsSection(
    rows: List<TrackerEntry>,
    imageIndex: Map<String, String>,
    onAdd: () -> Unit,
    onEdit: (TrackerEntry) -> Unit,
    onRepeat: (TrackerEntry) -> Unit,
    onDelete: (TrackerEntry) -> Unit
) {
    var filter by rememberSaveable { mutableStateOf(t("Όλα")) }
    var calendarMode by rememberSaveable { mutableStateOf(false) }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var selectedDayStart by rememberSaveable { mutableStateOf<Long?>(null) }
    val now = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L
    val todayCount = rows.count { it.createdAt >= now - dayMs }
    val weekCount = rows.count { it.createdAt >= now - 7L * dayMs }
    val filters = listOf(t("Όλα")) + rows.map { it.peptide }.distinct().sorted()
    val filteredByPeptide = if (filter == t("Όλα")) rows else rows.filter { it.peptide == filter }
    val visible = selectedDayStart?.let { start ->
        filteredByPeptide.filter { it.createdAt >= start && it.createdAt < start + dayMs }
    } ?: filteredByPeptide

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = t("Χρήσεις πεπτιδίων"),
                subtitle = rows.size.toString() + t(" συνολικά · ") + todayCount + t(" τελευταίο 24ωρο · ") + weekCount + t(" τελευταίες 7 ημέρες"),
                icon = Icons.AutoMirrored.Rounded.EventNote,
                accent = ElectricBlue,
                buttonText = t("Νέα χρήση"),
                onAdd = onAdd
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !calendarMode,
                    onClick = {
                        calendarMode = false
                        selectedDayStart = null
                    },
                    label = { Text(t("Λίστα")) },
                    modifier = Modifier.weight(1f),
                    colors = premiumFilterChipColors()
                )
                FilterChip(
                    selected = calendarMode,
                    onClick = { calendarMode = true },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(t("Ημερολόγιο"))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = premiumFilterChipColors()
                )
            }
        }

        if (calendarMode) {
            item {
                UsageCalendar(
                    rows = filteredByPeptide,
                    monthOffset = monthOffset,
                    selectedDayStart = selectedDayStart,
                    onPreviousMonth = {
                        monthOffset -= 1
                        selectedDayStart = null
                    },
                    onNextMonth = {
                        monthOffset += 1
                        selectedDayStart = null
                    },
                    onSelectDay = { selectedDayStart = it }
                )
            }
        }

        if (filters.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(filters) { item ->
                        FilterChip(
                            selected = filter == item,
                            onClick = { filter = item },
                            label = { Text(item, maxLines = 1) },
                            colors = premiumFilterChipColors()
                        )
                    }
                }
            }
        }

        if (visible.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = when {
                        rows.isEmpty() -> t("Δεν έχεις καταγράψει χρήση")
                        selectedDayStart != null -> t("Δεν υπάρχουν εγγραφές για αυτή την ημέρα")
                        else -> t("Δεν υπάρχουν εγγραφές για αυτό το φίλτρο")
                    },
                    text = when {
                        rows.isEmpty() -> t("Πρόσθεσε μια καταγραφή με πεπτίδιο, ποσότητα, μονάδα και ημερομηνία/ώρα.")
                        selectedDayStart != null -> t("Διάλεξε άλλη ημέρα ή γύρισε στη λίστα.")
                        else -> t("Διάλεξε άλλο πεπτίδιο ή επίλεξε «Όλα».")
                    },
                    icon = Icons.AutoMirrored.Rounded.EventNote
                )
            }
        } else {
            items(visible, key = { it.id }) { row ->
                LogCard(
                    row = row,
                    imageIndex = imageIndex,
                    onEdit = { onEdit(row) },
                    onRepeat = { onRepeat(row) },
                    onDelete = { onDelete(row) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun UsageCalendar(
    rows: List<TrackerEntry>,
    monthOffset: Int,
    selectedDayStart: Long?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, monthOffset)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val leading = (firstDayOfWeek + 5) % 7
    val totalCells = ((leading + daysInMonth + 6) / 7) * 7
    val dayCounts = rows.groupingBy { entry ->
        Calendar.getInstance().apply { timeInMillis = entry.createdAt }.let {
            Triple(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
        }
    }.eachCount()
    val monthLabel = SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(calendar.time)
    val weekDays = if (isGreekLanguage()) {
        listOf("Δ", "Τ", "Τ", "Π", "Π", "Σ", "Κ")
    } else {
        listOf("M", "T", "W", "T", "F", "S", "S")
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = t("Προηγούμενος μήνας"))
                }
                Text(
                    monthLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = t("Επόμενος μήνας"))
                }
            }

            Row(Modifier.fillMaxWidth()) {
                weekDays.forEach { label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            for (week in 0 until totalCells / 7) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (column in 0 until 7) {
                        val index = week * 7 + column
                        val day = index - leading + 1
                        if (day !in 1..daysInMonth) {
                            Spacer(Modifier.weight(1f).height(44.dp))
                        } else {
                            val dayStart = Calendar.getInstance().apply {
                                clear()
                                set(year, month, day, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            val count = dayCounts[Triple(year, month, day)] ?: 0
                            val selected = selectedDayStart == dayStart
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            selected -> ElectricBlue.copy(alpha = 0.30f)
                                            count > 0 -> ElectricCyan.copy(alpha = 0.10f)
                                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.52f)
                                        }
                                    )
                                    .clickable { onSelectDay(dayStart) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        day.toString(),
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                    if (count > 0) {
                                        Text(
                                            count.toString(),
                                            color = ElectricCyan,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventorySection(
    rows: List<InventoryEntry>,
    imageIndex: Map<String, String>,
    onAdd: () -> Unit,
    onActivate: (InventoryEntry) -> Unit,
    onEdit: (InventoryEntry) -> Unit,
    onDelete: (InventoryEntry) -> Unit,
    onCalculator: (InventoryEntry) -> Unit
) {
    val totalVials = rows.sumOf { it.quantity }
    val lowStock = rows.count { it.quantity <= 1 }
    val activeCount = rows.count { it.active }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = t("Απόθεμα φιαλιδίων"),
                subtitle = totalVials.toString() + t(" διαθέσιμα · ") + activeCount + t(" ενεργά · ") + lowStock + t(" χαμηλού αποθέματος"),
                icon = Icons.Rounded.Inventory2,
                accent = ElectricViolet,
                buttonText = t("Προσθήκη"),
                onAdd = onAdd
            )
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = t("Το απόθεμα είναι άδειο"),
                    text = t("Πρόσθεσε τα φιαλίδιά σου. Μπορείς μετά να ορίσεις ποιο είναι ενεργό και να βλέπεις την υπόλοιπη ποσότητα."),
                    icon = Icons.Rounded.Inventory2
                )
            }
        } else {
            items(rows, key = { it.id }) { row ->
                InventoryCard(
                    row = row,
                    imageIndex = imageIndex,
                    onActivate = { onActivate(row) },
                    onEdit = { onEdit(row) },
                    onDelete = { onDelete(row) },
                    onCalculator = { onCalculator(row) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ProgressSection(
    rows: List<ProgressEntry>,
    onAdd: () -> Unit,
    onEdit: (ProgressEntry) -> Unit,
    onDelete: (ProgressEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = t("Μετρήσεις σώματος"),
                subtitle = rows.size.toString() + t(" αποθηκευμένες μετρήσεις"),
                icon = Icons.Rounded.MonitorWeight,
                accent = ElectricCyan,
                buttonText = t("Μέτρηση"),
                onAdd = onAdd
            )
        }

        if (rows.size >= 2) {
            item { ProgressOverview(rows) }
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = t("Δεν έχεις αποθηκεύσει μετρήσεις"),
                    text = t("Πρόσθεσε βάρος και προαιρετικά περίμετρο μέσης για να βλέπεις την τάση με τον χρόνο."),
                    icon = Icons.Rounded.MonitorWeight
                )
            }
        } else {
            items(rows, key = { it.id }) { row ->
                ProgressCard(row, { onEdit(row) }, { onDelete(row) })
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatisticsSection(
    logs: List<TrackerEntry>,
    inventory: List<InventoryEntry>,
    progress: List<ProgressEntry>
) {
    val now = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L
    val weekLogs = logs.count { it.createdAt >= now - 7L * dayMs }
    val monthLogs = logs.count { it.createdAt >= now - 30L * dayMs }
    val distinctPeptides = logs.map { it.peptide }.distinct().size
    val usageCounts = logs
        .groupingBy { it.peptide }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
    val topUsage = usageCounts.firstOrNull()
    val maxUsage = usageCounts.firstOrNull()?.value?.coerceAtLeast(1) ?: 1
    val totalVials = inventory.sumOf { it.quantity }
    val activeVials = inventory.count { it.active && it.quantity > 0 }
    val reconstitutedVials = inventory.count { it.isReconstituted && it.quantity > 0 }
    val estimatedInventoryMg = inventory.sumOf { row ->
        if (row.quantity <= 0) {
            0.0
        } else if (row.active) {
            row.effectiveRemainingMg +
                (row.quantity - 1).coerceAtLeast(0) * row.vialMg
        } else {
            row.quantity * row.vialMg
        }
    }
    val newestWeight = progress.firstOrNull()?.weight
    val oldestWeight = progress.lastOrNull()?.weight
    val weightDelta = if (newestWeight != null && oldestWeight != null && progress.size >= 2) {
        newestWeight - oldestWeight
    } else {
        null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = t("Στατιστικά"),
                subtitle = t("Σύνοψη από τις δικές σου καταγραφές, το απόθεμα και τις μετρήσεις."),
                icon = Icons.Rounded.QueryStats,
                accent = ElectricBlue
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticsMetricCard(
                    value = weekLogs.toString(),
                    label = t("Χρήσεις 7ημ."),
                    modifier = Modifier.weight(1f)
                )
                StatisticsMetricCard(
                    value = monthLogs.toString(),
                    label = t("Χρήσεις 30ημ."),
                    modifier = Modifier.weight(1f)
                )
                StatisticsMetricCard(
                    value = distinctPeptides.toString(),
                    label = t("Πεπτίδια"),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        t("Δραστηριότητα ανά πεπτίδιο"),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (usageCounts.isEmpty()) {
                        Text(
                            t("Δεν υπάρχουν ακόμη καταγραφές χρήσης."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        usageCounts.take(5).forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        item.key,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        item.value.toString() + t(" καταγραφές"),
                                        color = ElectricCyan,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { item.value.toFloat() / maxUsage.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = ElectricCyan,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t("Απόθεμα"),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    StatisticsRow(t("Συνολικά vial"), totalVials.toString())
                    StatisticsRow(t("Ενεργά vial"), activeVials.toString())
                    StatisticsRow(t("Με καταχωρημένη ανασύσταση"), reconstitutedVials.toString())
                    StatisticsRow(
                        t("Εκτιμώμενη συνολική ποσότητα"),
                        formatCompact(estimatedInventoryMg) + " mg"
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t("Μετρήσεις σώματος"),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (newestWeight == null) {
                        Text(
                            t("Δεν υπάρχουν ακόμη μετρήσεις βάρους."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        StatisticsRow(t("Τελευταίο βάρος"), formatCompact(newestWeight) + " kg")
                        if (weightDelta != null) {
                            StatisticsRow(
                                t("Μεταβολή από πρώτη μέτρηση"),
                                (if (weightDelta > 0) "+" else "") +
                                    String.format("%.1f", weightDelta) + " kg"
                            )
                        }
                        StatisticsRow(t("Αποθηκευμένες μετρήσεις"), progress.size.toString())
                    }
                }
            }
        }

        if (topUsage != null) {
            item {
                Text(
                    t("Πιο συχνά καταγεγραμμένο: ") + topUsage.key +
                        " (" + topUsage.value + t(" καταγραφές). Τα στατιστικά περιγράφουν μόνο τα δεδομένα που έχεις αποθηκεύσει."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatisticsMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.heightIn(min = 90.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun StatisticsRow(
    label: String,
    value: String
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionHero(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    buttonText: String? = null,
    onAdd: (() -> Unit)? = null
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (buttonText != null && onAdd != null) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    colors = premiumButtonColors()
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
private fun EmptyTrackerState(
    title: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(40.dp))
            Text(title, fontWeight = FontWeight.ExtraBold)
            Text(
                text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LogCard(
    row: TrackerEntry,
    imageIndex: Map<String, String>,
    onEdit: () -> Unit,
    onRepeat: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onEdit) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StoreVialImage(
                    productKey = row.peptide,
                    imageIndex = imageIndex,
                    modifier = Modifier.size(width = 54.dp, height = 74.dp)
                )
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        row.peptide,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(row.amount, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    if (row.site.isNotBlank()) {
                        Text(
                            t("Σημείο: ") + row.site,
                            color = ElectricViolet,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (row.note.isNotBlank()) {
                        Text(
                            row.note,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        DateFormat.getDateTimeInstance(
                            DateFormat.SHORT,
                            DateFormat.SHORT
                        ).format(Date(row.createdAt)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRepeat, colors = premiumTextButtonColors()) {
                    Icon(
                        Icons.Rounded.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(t("Επανάληψη"))
                }
                TextButton(onClick = onEdit, colors = premiumTextButtonColors()) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(t("Επεξεργασία"))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = t("Διαγραφή"),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryCard(
    row: InventoryEntry,
    imageIndex: Map<String, String>,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCalculator: () -> Unit
) {
    val remaining = row.effectiveRemainingMg.coerceAtLeast(0.0)
    val fraction = if (row.vialMg > 0) (remaining / row.vialMg).coerceIn(0.0, 1.0).toFloat() else 0f

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StoreVialImage(
                    productKey = row.peptide,
                    imageIndex = imageIndex,
                    modifier = Modifier.size(width = 62.dp, height = 84.dp)
                )
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            row.peptide,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            when {
                                row.active -> t("ΕΝΕΡΓΟ")
                                row.quantity <= 0 -> t("ΑΔΕΙΟ")
                                else -> t("ΚΛΕΙΣΤΟ")
                            },
                            color = when {
                                row.active -> ElectricCyan
                                row.quantity <= 0 -> NeonRose
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        row.quantity.toString() + " × " + formatCompact(row.vialMg) + " mg",
                        color = ElectricViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        row.batch.ifBlank { t("Χωρίς παρτίδα") },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (row.vendor.isNotBlank()) {
                        Text(
                            "Source: " + row.vendor,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    row.expiryDate?.let { expiry ->
                        Text(
                            t("Λήξη: ") + DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(expiry)),
                            color = if (expiry < System.currentTimeMillis()) NeonRose else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (expiry < System.currentTimeMillis()) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    if (row.note.isNotBlank()) {
                        Text(
                            row.note,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (row.isReconstituted) {
                        Text(
                            t("Ανασύσταση: ") + formatCompact(row.diluentMl ?: 0.0) + " mL · U-" + row.syringeUnitsPerMl,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        row.mcgPerSyringeUnit?.let { mcgPerUnit ->
                            Text(
                                formatCompact(mcgPerUnit) + t(" mcg ανά μονάδα U-") + row.syringeUnitsPerMl,
                                color = ElectricViolet,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            t("Δεν έχει καταχωρηθεί ανασύσταση"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (row.active) {
                        Text(
                            t("Υπόλοιπο ενεργού φιαλιδίου: ") + formatCompact(remaining) + " mg",
                            color = ElectricCyan,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (row.active) {
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = ElectricCyan,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!row.active && row.quantity > 0) {
                    TextButton(
                        onClick = onActivate,
                        modifier = Modifier.weight(1f),
                        colors = premiumTextButtonColors()
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(t("Ενεργό"))
                    }
                }
                TextButton(
                    onClick = onCalculator,
                    modifier = Modifier.weight(1f),
                    colors = premiumTextButtonColors()
                ) {
                    Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t("Υπολογιστής"))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = t("Επεξεργασία"))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = t("Διαγραφή"))
                }
            }
        }
    }
}

@Composable
private fun ProgressOverview(rows: List<ProgressEntry>) {
    val newest = rows.first()
    val oldest = rows.last()
    val delta = newest.weight - oldest.weight

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        t("Τάση βάρους"),
                        color = ElectricCyan,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        formatCompact(newest.weight) + " kg",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    (if (delta > 0) "+" else "") + String.format("%.1f", delta) + " kg",
                    color = if (delta <= 0) ElectricCyan else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            ProgressSparkline(
                rows = rows,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
        }
    }
}

@Composable
private fun ProgressSparkline(
    rows: List<ProgressEntry>,
    modifier: Modifier = Modifier
) {
    val points = rows.asReversed().map { it.weight }

    Canvas(modifier) {
        if (points.size < 2) return@Canvas

        val minValue = points.minOrNull() ?: return@Canvas
        val maxValue = points.maxOrNull() ?: return@Canvas
        val range = max(0.1, maxValue - minValue)
        val stepX = size.width / (points.size - 1)

        val path = Path()
        points.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = ((value - minValue) / range).toFloat()
            val y = size.height - (normalized * size.height * 0.78f) - size.height * 0.11f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, ElectricCyan, style = Stroke(width = 5f, cap = StrokeCap.Round))

        points.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = ((value - minValue) / range).toFloat()
            val y = size.height - (normalized * size.height * 0.78f) - size.height * 0.11f
            drawCircle(ElectricBlue, radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable
private fun ProgressCard(
    row: ProgressEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    formatCompact(row.weight) + " kg",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (row.waist != null) {
                    Text(
                        t("Μέση: ") + formatCompact(row.waist) + " cm",
                        color = ElectricCyan,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (row.note.isNotBlank()) {
                    Text(
                        row.note,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(row.createdAt)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = t("Επεξεργασία"))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = t("Διαγραφή"))
            }
        }
    }
}

@Composable
private fun LogEditorDialog(
    current: TrackerEntry?,
    template: TrackerEntry?,
    inventory: List<InventoryEntry>,
    peptideNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String, Long, Long?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val defaultPeptide = current?.peptide
        ?: template?.peptide
        ?: inventory.firstOrNull { it.active }?.peptide
        ?: peptideNames.firstOrNull().orEmpty()

    var peptide by remember(current) { mutableStateOf(defaultPeptide) }
    var peptideMenu by remember { mutableStateOf(false) }
    var amount by remember(current) {
        mutableStateOf(
            current?.amountValue?.let(::formatCompact)
                ?: current?.amount?.substringBefore(" ")?.replace(',', '.')
                ?: template?.amountValue?.let(::formatCompact)
                ?: template?.amount?.substringBefore(" ")?.replace(',', '.')
                ?: ""
        )
    }
    var unit by remember(current) {
        mutableStateOf(
            current?.unit?.takeIf { it.isNotBlank() }
                ?: current?.amount?.substringAfter(" ", "")?.takeIf { it in listOf("mg", "mcg", "units") }
                ?: template?.unit?.takeIf { it.isNotBlank() }
                ?: template?.amount?.substringAfter(" ", "")?.takeIf { it in listOf("mg", "mcg", "units") }
                ?: "mg"
        )
    }
    var note by remember(current, template) { mutableStateOf(current?.note ?: template?.note.orEmpty()) }
    var site by remember(current, template) { mutableStateOf(current?.site ?: template?.site.orEmpty()) }
    var timestamp by remember(current) { mutableStateOf(current?.createdAt ?: System.currentTimeMillis()) }
    var subtractFromInventory by remember { mutableStateOf(false) }

    val value = amount.replace(',', '.').toDoubleOrNull()
    val activeInventory = inventory.firstOrNull {
        it.active && it.quantity > 0 && it.peptide.equals(peptide, ignoreCase = true)
    }
    val canSubtract = current == null && activeInventory != null && unit != "units"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(if (current == null) t("Νέα καταγραφή χρήσης") else t("Επεξεργασία καταγραφής")) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        t("Καταγράφεις μια χρήση που έχει ήδη αποφασιστεί. Το app δεν προτείνει ποσότητα."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { peptideMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            peptide.ifBlank { t("Επίλεξε πεπτίδιο") },
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.replace(',', '.') },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t("Ποσότητα")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = premiumTextFieldColors()
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("mg", "mcg", "units").forEach { item ->
                            FilterChip(
                                selected = unit == item,
                                onClick = {
                                    unit = item
                                    if (item == "units") subtractFromInventory = false
                                },
                                label = { Text(item) },
                                modifier = Modifier.weight(1f),
                                colors = premiumFilterChipColors()
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = site,
                        onValueChange = { site = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t("Σημείο / site (προαιρετικά)")) },
                        singleLine = true,
                        colors = premiumTextFieldColors()
                    )
                }
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t("Σημείωση (προαιρετικά)")) },
                        colors = premiumTextFieldColors()
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showDatePicker(context, timestamp) { timestamp = it }
                            },
                            modifier = Modifier.weight(1.18f),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                                ).format(Date(timestamp)),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                showTimePicker(context, timestamp) { timestamp = it }
                            },
                            modifier = Modifier.weight(0.82f),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault()
                                ).format(Date(timestamp)),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
                if (activeInventory != null) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(t("Σύνδεση με ενεργό φιαλίδιο"), fontWeight = FontWeight.Bold)
                                Text(
                                    if (canSubtract) {
                                        t("Προαιρετικά αφαίρεσε την ποσότητα από το ενεργό φιαλίδιο.")
                                    } else {
                                        t("Η αυτόματη αφαίρεση γίνεται μόνο για mg ή mcg και μόνο σε νέα εγγραφή.")
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = subtractFromInventory && canSubtract,
                                onCheckedChange = { if (canSubtract) subtractFromInventory = it },
                                enabled = canSubtract
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        peptide.trim(),
                        value ?: return@Button,
                        unit,
                        note.trim(),
                        site.trim(),
                        timestamp,
                        activeInventory?.id,
                        subtractFromInventory && canSubtract
                    )
                },
                enabled = peptide.isNotBlank() && value != null && value > 0,
                colors = premiumButtonColors()
            ) {
                Text(if (current == null) t("Καταγραφή") else t("Αποθήκευση"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text(t("Άκυρο"))
            }
        }
    )

    if (peptideMenu) {
        PeptidePickerDialog(
            selectedPeptide = peptide,
            peptides = peptideNames,
            onDismiss = { peptideMenu = false },
            onSelect = { selected ->
                peptide = selected
                peptideMenu = false
                subtractFromInventory = false
            }
        )
    }

}

@Composable
private fun InventoryEditorDialog(
    current: InventoryEntry?,
    presetPeptide: String?,
    peptideNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String, Double?, Double?, Int, String, String, Long?, Long?) -> Unit
) {
    val context = LocalContext.current
    var peptide by remember(current, presetPeptide) {
        mutableStateOf(current?.peptide ?: presetPeptide ?: peptideNames.firstOrNull().orEmpty())
    }
    var peptideMenu by remember { mutableStateOf(false) }
    var vial by remember(current) { mutableStateOf(current?.vialMg?.let(::formatCompact) ?: "10") }
    var quantity by remember(current) { mutableStateOf(current?.quantity?.toString() ?: "1") }
    var batch by remember(current) { mutableStateOf(current?.batch.orEmpty()) }
    var vendor by remember(current) { mutableStateOf(current?.vendor.orEmpty()) }
    var inventoryNote by remember(current) { mutableStateOf(current?.note.orEmpty()) }
    var purchaseDate by remember(current) { mutableStateOf(current?.purchaseDate) }
    var expiryDate by remember(current) { mutableStateOf(current?.expiryDate) }
    var diluent by remember(current) {
        mutableStateOf(current?.diluentMl?.let(::formatCompact).orEmpty())
    }
    var syringeUnitsPerMl by remember(current) {
        mutableIntStateOf(current?.syringeUnitsPerMl ?: 100)
    }
    var remaining by remember(current) {
        mutableStateOf(current?.remainingMg?.let(::formatCompact) ?: current?.vialMg?.let(::formatCompact).orEmpty())
    }

    val vialValue = vial.replace(',', '.').toDoubleOrNull()
    val quantityValue = quantity.toIntOrNull()
    val diluentValue = diluent.replace(',', '.').toDoubleOrNull()
    val remainingValue = remaining.replace(',', '.').toDoubleOrNull()
    val purchaseDateValue = purchaseDate
    val expiryDateValue = expiryDate

    val inventoryValidationMessage = when {
        current == null && quantityValue != null && quantityValue <= 0 ->
            t("Χρειάζεται τουλάχιστον ένα φιαλίδιο.")
        purchaseDateValue != null &&
            expiryDateValue != null &&
            expiryDateValue < purchaseDateValue ->
            t("Η ημερομηνία λήξης δεν μπορεί να είναι πριν από την ημερομηνία αγοράς.")
        current != null &&
            remainingValue != null &&
            vialValue != null &&
            remainingValue > vialValue ->
            t("Το υπόλοιπο δεν μπορεί να είναι μεγαλύτερο από την περιεκτικότητα του φιαλιδίου.")
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(if (current == null) t("Προσθήκη στο απόθεμα") else t("Επεξεργασία αποθέματος")) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { peptideMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        peptide.ifBlank { t("Επίλεξε πεπτίδιο") },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedTextField(
                    value = vial,
                    onValueChange = { vial = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Περιεκτικότητα κάθε φιαλιδίου (mg)")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Πλήθος φιαλιδίων")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Lot / batch (προαιρετικά)")) },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Vendor / source (προαιρετικά)")) },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showDatePicker(
                                context,
                                purchaseDate ?: System.currentTimeMillis()
                            ) { purchaseDate = it }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            purchaseDate?.let {
                                t("Αγορά: ") + DateFormat.getDateInstance(DateFormat.SHORT).format(Date(it))
                            } ?: t("Ημ/νία αγοράς")
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            showDatePicker(
                                context,
                                expiryDate ?: System.currentTimeMillis()
                            ) { expiryDate = it }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            expiryDate?.let {
                                t("Λήξη: ") + DateFormat.getDateInstance(DateFormat.SHORT).format(Date(it))
                            } ?: t("Ημ/νία λήξης")
                        )
                    }
                }
                if (purchaseDate != null || expiryDate != null) {
                    TextButton(
                        onClick = {
                            purchaseDate = null
                            expiryDate = null
                        },
                        colors = premiumTextButtonColors()
                    ) {
                        Text(t("Καθαρισμός ημερομηνιών"))
                    }
                }
                OutlinedTextField(
                    value = inventoryNote,
                    onValueChange = { inventoryNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Σημειώσεις αποθέματος (προαιρετικά)")) },
                    colors = premiumTextFieldColors()
                )
                Text(
                    t("Ανασύσταση φιαλιδίου (προαιρετικά)"),
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = diluent,
                    onValueChange = { diluent = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Διαλύτης που προστέθηκε (mL)")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 40).forEach { unitsPerMl ->
                        FilterChip(
                            selected = syringeUnitsPerMl == unitsPerMl,
                            onClick = { syringeUnitsPerMl = unitsPerMl },
                            label = { Text("U-" + unitsPerMl) },
                            modifier = Modifier.weight(1f),
                            colors = premiumFilterChipColors()
                        )
                    }
                }
                Text(
                    if (diluentValue != null && diluentValue > 0 && vialValue != null && vialValue > 0) {
                        val mcgPerUnit = (vialValue / diluentValue) * 1000.0 / syringeUnitsPerMl
                        t("Συγκέντρωση: ") + formatCompact(vialValue / diluentValue) +
                            " mg/mL · " + formatCompact(mcgPerUnit) + t(" mcg/μονάδα")
                    } else {
                        t("Άφησέ το κενό αν το vial δεν έχει ανασυσταθεί ακόμη.")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (current != null) {
                    OutlinedTextField(
                        value = remaining,
                        onValueChange = { remaining = it.replace(',', '.') },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t("Υπόλοιπο ενεργού vial (mg)")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = premiumTextFieldColors()
                    )
                }
                if (inventoryValidationMessage != null) {
                    Text(
                        inventoryValidationMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        peptide.trim(),
                        vialValue ?: return@Button,
                        quantityValue ?: return@Button,
                        batch.trim(),
                        if (current == null) null else remainingValue,
                        diluentValue,
                        syringeUnitsPerMl,
                        vendor.trim(),
                        inventoryNote.trim(),
                        purchaseDate,
                        expiryDate
                    )
                },
                enabled = peptide.isNotBlank() &&
                    vialValue != null && vialValue > 0 &&
                    quantityValue != null &&
                    (if (current == null) quantityValue > 0 else quantityValue >= 0) &&
                    (diluent.isBlank() || (diluentValue != null && diluentValue > 0)) &&
                    (current == null || remainingValue == null || remainingValue >= 0) &&
                    inventoryValidationMessage == null,
                colors = premiumButtonColors()
            ) {
                Text(t("Αποθήκευση"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text(t("Άκυρο"))
            }
        }
    )

    if (peptideMenu) {
        PeptidePickerDialog(
            selectedPeptide = peptide,
            peptides = peptideNames,
            onDismiss = { peptideMenu = false },
            onSelect = { selected ->
                peptide = selected
                peptideMenu = false
            }
        )
    }

}

@Composable
private fun ProgressEditorDialog(
    current: ProgressEntry?,
    onDismiss: () -> Unit,
    onSave: (Double, Double?, String, Long) -> Unit
) {
    val context = LocalContext.current
    var weight by remember(current) { mutableStateOf(current?.weight?.let(::formatCompact).orEmpty()) }
    var waist by remember(current) { mutableStateOf(current?.waist?.let(::formatCompact).orEmpty()) }
    var note by remember(current) { mutableStateOf(current?.note.orEmpty()) }
    var timestamp by remember(current) { mutableStateOf(current?.createdAt ?: System.currentTimeMillis()) }

    val weightValue = weight.replace(',', '.').toDoubleOrNull()
    val waistValue = waist.replace(',', '.').toDoubleOrNull()

    val waistError = waist.isNotBlank() && (waistValue == null || waistValue <= 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(if (current == null) t("Νέα μέτρηση σώματος") else t("Επεξεργασία μέτρησης")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Βάρος (kg)")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Περίμετρος μέσης (cm, προαιρετικά)")) },
                    singleLine = true,
                    isError = waistError,
                    supportingText = {
                        if (waistError) {
                            Text(t("Η περίμετρος μέσης πρέπει να είναι θετικός αριθμός."))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Σημείωση")) },
                    colors = premiumTextFieldColors()
                )
                if (current != null) {
                    OutlinedButton(
                        onClick = { showDatePicker(context, timestamp) { timestamp = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp)))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(weightValue ?: return@Button, waistValue, note.trim(), timestamp)
                },
                enabled = weightValue != null && weightValue > 0 && !waistError,
                colors = premiumButtonColors()
            ) {
                Text(t("Αποθήκευση"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text(t("Άκυρο"))
            }
        }
    )
}

private fun showDatePicker(context: Context, timestamp: Long, onChanged: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            onChanged(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePicker(context: Context, timestamp: Long, onChanged: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            onChanged(calendar.timeInMillis)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

private fun formatCompact(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else value.toString().trimEnd('0').trimEnd('.')
