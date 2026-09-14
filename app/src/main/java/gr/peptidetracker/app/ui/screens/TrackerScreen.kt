package gr.peptidetracker.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.InventoryEntry
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.ProgressEntry
import gr.peptidetracker.app.data.TrackerEntry
import gr.peptidetracker.app.data.peptideCatalog
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
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.max

@Composable
fun TrackerScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    initialSection: Int = 0,
    newLogRequest: Int = 0,
    inventoryPeptidePreset: String? = null,
    onConsumeNewLogRequest: () -> Unit = {},
    onConsumeInventoryPreset: () -> Unit = {},
    onOpenCalculator: (InventoryEntry) -> Unit = {}
) {
    var section by remember { mutableIntStateOf(initialSection.coerceIn(0, 3)) }
    var logs by remember { mutableStateOf(store.entries()) }
    var inventory by remember { mutableStateOf(store.inventory()) }
    var progress by remember { mutableStateOf(store.progress()) }

    var editingLog by remember { mutableStateOf<TrackerEntry?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }
    var editingInventory by remember { mutableStateOf<InventoryEntry?>(null) }
    var inventoryPreset by remember { mutableStateOf<String?>(null) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var editingProgress by remember { mutableStateOf<ProgressEntry?>(null) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showDataTools by remember { mutableStateOf(false) }
    var dataMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    fun reloadAll() {
        logs = store.entries()
        inventory = store.inventory()
        progress = store.progress()
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(store.exportJson())
                } ?: error("Δεν ήταν δυνατή η εγγραφή του αρχείου.")
            }.onSuccess {
                dataMessage = "Το backup αποθηκεύτηκε."
            }.onFailure {
                dataMessage = "Αποτυχία αποθήκευσης backup."
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
                } ?: error("Δεν ήταν δυνατή η εγγραφή του αρχείου.")
            }.onSuccess {
                dataMessage = "Το CSV αποθηκεύτηκε."
            }.onFailure {
                dataMessage = "Αποτυχία εξαγωγής CSV."
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Κενό αρχείο.")
                check(store.restoreJson(raw))
            }.onSuccess {
                reloadAll()
                dataMessage = "Το backup επαναφέρθηκε επιτυχώς."
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }.onFailure {
                dataMessage = "Το αρχείο backup δεν είναι έγκυρο."
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
                title = "Ημερολόγιο",
                subtitle = "Χρήσεις, ενεργά φιαλίδια, απόθεμα και προσωπικές μετρήσεις."
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                TrackerTab("Χρήσεις", section == 0, Modifier.weight(1f)) { section = 0 }
                TrackerTab("Απόθεμα", section == 1, Modifier.weight(1f)) { section = 1 }
                TrackerTab("Μετρήσεις", section == 2, Modifier.weight(1f)) { section = 2 }
                TrackerTab("Στατ.", section == 3, Modifier.weight(1f)) { section = 3 }
            }

            TextButton(
                onClick = { showDataTools = true },
                colors = premiumTextButtonColors()
            ) {
                Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Backup, restore & export")
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
                    showLogDialog = true
                },
                onEdit = {
                    editingLog = it
                    showLogDialog = true
                },
                onDelete = {
                    store.deleteEntry(it.id)
                    logs = store.entries()
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
                onDelete = {
                    store.deleteInventory(it.id)
                    inventory = store.inventory()
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
                    store.deleteProgress(it.id)
                    progress = store.progress()
                }
            )

            else -> StatisticsSection(
                logs = logs,
                inventory = inventory,
                progress = progress
            )
        }
    }

    if (showLogDialog) {
        LogEditorDialog(
            current = editingLog,
            inventory = inventory,
            onDismiss = { showLogDialog = false },
            onSave = { peptide, value, unit, note, site, createdAt, inventoryId, subtract ->
                val current = editingLog
                if (current == null) {
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
                reloadAll()
                showLogDialog = false
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
    }

    if (showInventoryDialog) {
        InventoryEditorDialog(
            current = editingInventory,
            presetPeptide = inventoryPreset,
            onDismiss = { showInventoryDialog = false },
            onSave = { peptide, vial, quantity, batch, remaining, diluentMl, syringeUnitsPerMl ->
                val current = editingInventory
                if (current == null) {
                    store.addInventory(
                        peptide = peptide,
                        vial = vial,
                        quantity = quantity,
                        batch = batch,
                        diluentMl = diluentMl,
                        syringeUnitsPerMl = syringeUnitsPerMl
                    )
                } else {
                    store.updateInventory(
                        id = current.id,
                        peptide = peptide,
                        vialMg = vial,
                        quantity = quantity,
                        batch = batch,
                        remainingMg = remaining,
                        diluentMl = diluentMl,
                        syringeUnitsPerMl = syringeUnitsPerMl
                    )
                }
                inventory = store.inventory()
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
            title = { Text("Δεδομένα εφαρμογής") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Το backup περιλαμβάνει ημερολόγιο, απόθεμα, μετρήσεις, αγαπημένα και βασικές ρυθμίσεις. Όλα παραμένουν τοπικά εκτός αν εσύ αποθηκεύσεις ή μοιραστείς το αρχείο.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            showDataTools = false
                            backupLauncher.launch("PeptideTrackerGR-backup.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = premiumButtonColors()
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text("Αποθήκευση backup")
                    }
                    OutlinedButton(
                        onClick = {
                            showDataTools = false
                            restoreLauncher.launch(arrayOf("application/json", "text/plain"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text("Επαναφορά backup")
                    }
                    OutlinedButton(
                        onClick = {
                            showDataTools = false
                            csvLauncher.launch("PeptideTrackerGR-history.csv")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Εξαγωγή ημερολογίου σε CSV")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataTools = false }, colors = premiumTextButtonColors()) {
                    Text("Κλείσιμο")
                }
            }
        )
    }
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
    onDelete: (TrackerEntry) -> Unit
) {
    var filter by remember { mutableStateOf("Όλα") }
    val now = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L
    val todayCount = rows.count { it.createdAt >= now - dayMs }
    val weekCount = rows.count { it.createdAt >= now - 7L * dayMs }
    val filters = listOf("Όλα") + rows.map { it.peptide }.distinct().sorted()
    val visible = if (filter == "Όλα") rows else rows.filter { it.peptide == filter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = "Χρήσεις πεπτιδίων",
                subtitle = rows.size.toString() + " συνολικά · " + todayCount + " τελευταίο 24ωρο · " + weekCount + " τελευταίες 7 ημέρες",
                icon = Icons.AutoMirrored.Rounded.EventNote,
                accent = ElectricBlue,
                buttonText = "Νέα χρήση",
                onAdd = onAdd
            )
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
                    title = if (rows.isEmpty()) "Δεν έχεις καταγράψει χρήση" else "Δεν υπάρχουν εγγραφές για αυτό το φίλτρο",
                    text = if (rows.isEmpty()) {
                        "Πρόσθεσε μια καταγραφή με πεπτίδιο, ποσότητα, μονάδα και ημερομηνία/ώρα."
                    } else {
                        "Διάλεξε άλλο πεπτίδιο ή επίλεξε «Όλα»."
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
                    onDelete = { onDelete(row) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
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
                title = "Απόθεμα φιαλιδίων",
                subtitle = totalVials.toString() + " διαθέσιμα · " + activeCount + " ενεργά · " + lowStock + " χαμηλού αποθέματος",
                icon = Icons.Rounded.Inventory2,
                accent = ElectricViolet,
                buttonText = "Προσθήκη",
                onAdd = onAdd
            )
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = "Το απόθεμα είναι άδειο",
                    text = "Πρόσθεσε τα φιαλίδιά σου. Μπορείς μετά να ορίσεις ποιο είναι ενεργό και να βλέπεις την υπόλοιπη ποσότητα.",
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
                title = "Μετρήσεις σώματος",
                subtitle = rows.size.toString() + " αποθηκευμένες μετρήσεις",
                icon = Icons.Rounded.MonitorWeight,
                accent = ElectricCyan,
                buttonText = "Μέτρηση",
                onAdd = onAdd
            )
        }

        if (rows.size >= 2) {
            item { ProgressOverview(rows) }
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = "Δεν έχεις αποθηκεύσει μετρήσεις",
                    text = "Πρόσθεσε βάρος και προαιρετικά περίμετρο μέσης για να βλέπεις την τάση με τον χρόνο.",
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
        } else {
            val activeCount = if (row.active) 1 else 0
            val unopenedCount = (row.quantity - activeCount).coerceAtLeast(0)
            (if (row.active) row.effectiveRemainingMg else 0.0) +
                unopenedCount * row.vialMg +
                (if (!row.active) row.vialMg else 0.0)
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
                title = "Στατιστικά",
                subtitle = "Σύνοψη από τις δικές σου καταγραφές, το απόθεμα και τις μετρήσεις.",
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
                    label = "Χρήσεις 7ημ.",
                    modifier = Modifier.weight(1f)
                )
                StatisticsMetricCard(
                    value = monthLogs.toString(),
                    label = "Χρήσεις 30ημ.",
                    modifier = Modifier.weight(1f)
                )
                StatisticsMetricCard(
                    value = distinctPeptides.toString(),
                    label = "Πεπτίδια",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Δραστηριότητα ανά πεπτίδιο",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (usageCounts.isEmpty()) {
                        Text(
                            "Δεν υπάρχουν ακόμη καταγραφές χρήσης.",
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
                                        item.value.toString() + " καταγραφές",
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
                                    trackColor = Color.White.copy(alpha = 0.09f)
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
                        "Απόθεμα",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    StatisticsRow("Συνολικά vial", totalVials.toString())
                    StatisticsRow("Ενεργά vial", activeVials.toString())
                    StatisticsRow("Με καταχωρημένη ανασύσταση", reconstitutedVials.toString())
                    StatisticsRow(
                        "Εκτιμώμενη συνολική ποσότητα",
                        formatCompact(estimatedInventoryMg) + " mg"
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Μετρήσεις σώματος",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (newestWeight == null) {
                        Text(
                            "Δεν υπάρχουν ακόμη μετρήσεις βάρους.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        StatisticsRow("Τελευταίο βάρος", formatCompact(newestWeight) + " kg")
                        if (weightDelta != null) {
                            StatisticsRow(
                                "Μεταβολή από πρώτη μέτρηση",
                                (if (weightDelta > 0) "+" else "") +
                                    String.format("%.1f", weightDelta) + " kg"
                            )
                        }
                        StatisticsRow("Αποθηκευμένες μετρήσεις", progress.size.toString())
                    }
                }
            }
        }

        if (topUsage != null) {
            item {
                Text(
                    "Πιο συχνά καταγεγραμμένο: " + topUsage.key +
                        " (" + topUsage.value + " καταγραφές). Τα στατιστικά περιγράφουν μόνο τα δεδομένα που έχεις αποθηκεύσει.",
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
        modifier = modifier.height(90.dp),
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
            if (buttonText != null && onAdd != null) {
                Button(onClick = onAdd, colors = premiumButtonColors()) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
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
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onEdit) {
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
                        "Σημείο: " + row.site,
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
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(row.createdAt)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Επεξεργασία")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Διαγραφή",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        if (row.active) {
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "ΕΝΕΡΓΟ",
                                color = ElectricCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else if (row.quantity <= 1) {
                            Spacer(Modifier.width(7.dp))
                            Icon(
                                Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = NeonRose,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        row.quantity.toString() + " × " + formatCompact(row.vialMg) + " mg",
                        color = ElectricViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        row.batch.ifBlank { "Χωρίς παρτίδα" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (row.isReconstituted) {
                        Text(
                            "Ανασύσταση: " + formatCompact(row.diluentMl ?: 0.0) + " mL · U-" + row.syringeUnitsPerMl,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        row.mcgPerSyringeUnit?.let { mcgPerUnit ->
                            Text(
                                formatCompact(mcgPerUnit) + " mcg ανά μονάδα U-" + row.syringeUnitsPerMl,
                                color = ElectricViolet,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            "Δεν έχει καταχωρηθεί ανασύσταση",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (row.active) {
                        Text(
                            "Υπόλοιπο ενεργού vial: " + formatCompact(remaining) + " mg",
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
                    trackColor = Color.White.copy(alpha = 0.09f)
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
                        Text("Ενεργό")
                    }
                }
                TextButton(
                    onClick = onCalculator,
                    modifier = Modifier.weight(1f),
                    colors = premiumTextButtonColors()
                ) {
                    Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Calculator")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Επεξεργασία")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Διαγραφή")
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
                        "Τάση βάρους",
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
                        "Μέση: " + formatCompact(row.waist) + " cm",
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
                Icon(Icons.Outlined.Edit, contentDescription = "Επεξεργασία")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Διαγραφή")
            }
        }
    }
}

@Composable
private fun LogEditorDialog(
    current: TrackerEntry?,
    inventory: List<InventoryEntry>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String, Long, Long?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val defaultPeptide = current?.peptide
        ?: inventory.firstOrNull { it.active }?.peptide
        ?: peptideCatalog.firstOrNull()?.name.orEmpty()

    var peptide by remember(current) { mutableStateOf(defaultPeptide) }
    var peptideMenu by remember { mutableStateOf(false) }
    var amount by remember(current) {
        mutableStateOf(
            current?.amountValue?.let(::formatCompact)
                ?: current?.amount?.substringBefore(" ")?.replace(',', '.')
                ?: "100"
        )
    }
    var unit by remember(current) {
        mutableStateOf(
            current?.unit?.takeIf { it.isNotBlank() }
                ?: current?.amount?.substringAfter(" ", "")?.takeIf { it in listOf("mg", "mcg", "units") }
                ?: "mcg"
        )
    }
    var note by remember(current) { mutableStateOf(current?.note.orEmpty()) }
    var site by remember(current) { mutableStateOf(current?.site.orEmpty()) }
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
        title = { Text(if (current == null) "Νέα καταγραφή χρήσης" else "Επεξεργασία καταγραφής") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Καταγράφεις μια χρήση που έχει ήδη αποφασιστεί. Το app δεν προτείνει ποσότητα.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item {
                    Box {
                        OutlinedButton(
                            onClick = { peptideMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(peptide.ifBlank { "Επίλεξε πεπτίδιο" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(
                            expanded = peptideMenu,
                            onDismissRequest = { peptideMenu = false }
                        ) {
                            peptideCatalog.sortedBy { it.name }.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        peptide = item.name
                                        peptideMenu = false
                                        subtractFromInventory = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.replace(',', '.') },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ποσότητα") },
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
                        listOf("mcg", "mg", "units").forEach { item ->
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
                        label = { Text("Σημείο / site (προαιρετικά)") },
                        singleLine = true,
                        colors = premiumTextFieldColors()
                    )
                }
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Σημείωση (προαιρετικά)") },
                        colors = premiumTextFieldColors()
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker(context, timestamp) { timestamp = it } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(timestamp)))
                        }
                        OutlinedButton(
                            onClick = { showTimePicker(context, timestamp) { timestamp = it } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp)))
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
                                Text("Σύνδεση με ενεργό vial", fontWeight = FontWeight.Bold)
                                Text(
                                    if (canSubtract) {
                                        "Προαιρετικά αφαίρεσε την ποσότητα από το ενεργό vial."
                                    } else {
                                        "Η αυτόματη αφαίρεση γίνεται μόνο για mg ή mcg και μόνο σε νέα εγγραφή."
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
                Text(if (current == null) "Καταγραφή" else "Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text("Άκυρο")
            }
        }
    )
}

@Composable
private fun InventoryEditorDialog(
    current: InventoryEntry?,
    presetPeptide: String?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String, Double?, Double?, Int) -> Unit
) {
    var peptide by remember(current, presetPeptide) {
        mutableStateOf(current?.peptide ?: presetPeptide ?: peptideCatalog.firstOrNull()?.name.orEmpty())
    }
    var peptideMenu by remember { mutableStateOf(false) }
    var vial by remember(current) { mutableStateOf(current?.vialMg?.let(::formatCompact) ?: "10") }
    var quantity by remember(current) { mutableStateOf(current?.quantity?.toString() ?: "1") }
    var batch by remember(current) { mutableStateOf(current?.batch.orEmpty()) }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(if (current == null) "Προσθήκη στο απόθεμα" else "Επεξεργασία αποθέματος") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    OutlinedButton(
                        onClick = { peptideMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(peptide, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = peptideMenu,
                        onDismissRequest = { peptideMenu = false }
                    ) {
                        peptideCatalog.sortedBy { it.name }.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    peptide = item.name
                                    peptideMenu = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = vial,
                    onValueChange = { vial = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Περιεκτικότητα κάθε vial (mg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Πλήθος φιαλιδίων") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lot / batch (προαιρετικά)") },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
                Text(
                    "Ανασύσταση vial (προαιρετικά)",
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = diluent,
                    onValueChange = { diluent = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Διαλύτης που προστέθηκε (mL)") },
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
                        "Συγκέντρωση: " + formatCompact(vialValue / diluentValue) +
                            " mg/mL · " + formatCompact(mcgPerUnit) + " mcg/μονάδα"
                    } else {
                        "Άφησέ το κενό αν το vial δεν έχει ανασυσταθεί ακόμη."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (current != null) {
                    OutlinedTextField(
                        value = remaining,
                        onValueChange = { remaining = it.replace(',', '.') },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Υπόλοιπο ενεργού vial (mg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = premiumTextFieldColors()
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
                        syringeUnitsPerMl
                    )
                },
                enabled = peptide.isNotBlank() &&
                    vialValue != null && vialValue > 0 &&
                    quantityValue != null && quantityValue >= 0 &&
                    (diluent.isBlank() || (diluentValue != null && diluentValue > 0)) &&
                    (current == null || remainingValue == null || remainingValue >= 0),
                colors = premiumButtonColors()
            ) {
                Text("Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text("Άκυρο")
            }
        }
    )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text(if (current == null) "Νέα μέτρηση σώματος" else "Επεξεργασία μέτρησης") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Βάρος (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it.replace(',', '.') },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Περίμετρος μέσης (cm, προαιρετικά)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Σημείωση") },
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
                enabled = weightValue != null && weightValue > 0,
                colors = premiumButtonColors()
            ) {
                Text("Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = premiumTextButtonColors()) {
                Text("Άκυρο")
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
