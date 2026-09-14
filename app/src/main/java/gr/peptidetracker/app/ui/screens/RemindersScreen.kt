package gr.peptidetracker.app.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.ReminderEntry
import gr.peptidetracker.app.data.ReminderScheduler
import gr.peptidetracker.app.data.peptideCatalog
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.GlassSurfaceStrong
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

@Composable
fun RemindersScreen(
    store: LocalStore,
    imageIndex: Map<String, String>
) {
    val context = LocalContext.current
    var rows by remember { mutableStateOf(store.reminders()) }
    var editing by remember { mutableStateOf<ReminderEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var openEditorAfterPermission by remember { mutableStateOf(false) }
    var pendingEnableId by remember { mutableStateOf<Long?>(null) }

    fun notificationsAllowed(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun reload() {
        rows = store.reminders()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            message = "Οι ειδοποιήσεις ενεργοποιήθηκαν."
            pendingEnableId?.let { id ->
                store.setReminderEnabled(id, true)?.let {
                    ReminderScheduler.schedule(context, it)
                }
            }
        } else {
            message = "Χωρίς άδεια ειδοποιήσεων οι υπενθυμίσεις αποθηκεύονται αλλά παραμένουν ανενεργές."
        }

        pendingEnableId = null
        reload()

        if (openEditorAfterPermission) {
            editing = null
            showEditor = true
            openEditorAfterPermission = false
        }
    }

    LaunchedEffect(Unit) {
        ReminderScheduler.rescheduleAll(context, rows)
    }

    val enabledCount = rows.count { it.enabled }
    val nextReminder = rows
        .filter { it.enabled && it.scheduledAt >= System.currentTimeMillis() }
        .minByOrNull { it.scheduledAt }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Πλάνο & Υπενθυμίσεις",
                subtitle = "Όρισε δικές σου ημερομηνίες και ώρες. Οι ειδοποιήσεις μπορεί να καθυστερήσουν ελαφρώς από το Android για εξοικονόμηση ενέργειας."
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(ElectricBlue.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            enabledCount.toString() + " ενεργές",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            if (nextReminder == null) {
                                "Δεν υπάρχει επόμενη ενεργή υπενθύμιση."
                            } else {
                                "Επόμενη: " + DateFormat.getDateTimeInstance(
                                    DateFormat.SHORT,
                                    DateFormat.SHORT
                                ).format(Date(nextReminder.scheduledAt))
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            editing = null
                            if (notificationsAllowed()) {
                                showEditor = true
                            } else {
                                openEditorAfterPermission = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = premiumButtonColors()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Νέα")
                    }
                }
            }
        }

        if (!notificationsAllowed()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Οι ειδοποιήσεις είναι κλειστές", fontWeight = FontWeight.ExtraBold)
                            Text(
                                "Οι υπενθυμίσεις μπορούν να αποθηκευτούν, αλλά δεν θα εμφανιστούν ως Android notifications.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = premiumTextButtonColors()
                        ) {
                            Text("Άδεια")
                        }
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            item {
                Text(
                    message,
                    color = ElectricCyan,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        if (rows.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(42.dp)
                        )
                        Text("Δεν έχεις υπενθυμίσεις", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Πρόσθεσε ημερομηνία και ώρα για κάτι που έχεις ήδη αποφασίσει να καταγράψεις. Δεν δημιουργούνται αυτόματα πρωτόκολλα ή δοσολογίες.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            items(rows, key = { it.id }) { row ->
                ReminderCard(
                    row = row,
                    imageIndex = imageIndex,
                    onToggle = { enabled ->
                        if (enabled && !notificationsAllowed()) {
                            pendingEnableId = row.id
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            store.setReminderEnabled(row.id, enabled)?.let { updated ->
                                if (updated.enabled) {
                                    ReminderScheduler.schedule(context, updated)
                                } else {
                                    ReminderScheduler.cancel(context, updated.id)
                                }
                            }
                            reload()
                        }
                    },
                    onEdit = {
                        editing = row
                        showEditor = true
                    },
                    onDelete = {
                        ReminderScheduler.cancel(context, row.id)
                        store.deleteReminder(row.id)
                        reload()
                    }
                )
            }
        }

        item {
            Text(
                "Οι υπενθυμίσεις είναι προσωπικές καταχωρήσεις χρόνου. Η εφαρμογή δεν επιλέγει πεπτίδιο, ποσότητα ή συχνότητα για εσένα και δεν εγγυάται εκτέλεση στο ακριβές δευτερόλεπτο.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showEditor) {
        ReminderEditorDialog(
            current = editing,
            notificationsAllowed = notificationsAllowed(),
            onDismiss = { showEditor = false },
            onSave = { peptide, note, scheduledAt, repeatDays, requestedEnabled ->
                val shouldEnable = requestedEnabled && notificationsAllowed()
                val current = editing

                val saved = if (current == null) {
                    store.addReminder(
                        peptide = peptide,
                        note = note,
                        scheduledAt = scheduledAt,
                        repeatDays = repeatDays
                    ).let { created ->
                        if (shouldEnable) created else store.setReminderEnabled(created.id, false) ?: created.copy(enabled = false)
                    }
                } else {
                    store.updateReminder(
                        id = current.id,
                        peptide = peptide,
                        note = note,
                        scheduledAt = scheduledAt,
                        repeatDays = repeatDays,
                        enabled = shouldEnable
                    ) ?: current
                }

                if (saved.enabled) {
                    ReminderScheduler.schedule(context, saved)
                } else {
                    ReminderScheduler.cancel(context, saved.id)
                }

                if (requestedEnabled && !notificationsAllowed()) {
                    message = "Η υπενθύμιση αποθηκεύτηκε ανενεργή μέχρι να επιτρέψεις notifications."
                } else {
                    message = "Η υπενθύμιση αποθηκεύτηκε."
                }

                showEditor = false
                reload()
            }
        )
    }
}

@Composable
private fun ReminderCard(
    row: ReminderEntry,
    imageIndex: Map<String, String>,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onEdit) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StoreVialImage(
                productKey = row.peptide,
                imageIndex = imageIndex,
                modifier = Modifier.size(width = 48.dp, height = 66.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    row.peptide,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    DateFormat.getDateTimeInstance(
                        DateFormat.MEDIUM,
                        DateFormat.SHORT
                    ).format(Date(row.scheduledAt)),
                    color = if (row.enabled) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (row.repeatDays) {
                        1 -> "Επανάληψη κάθε ημέρα"
                        7 -> "Επανάληψη κάθε 7 ημέρες"
                        0 -> "Μία φορά"
                        else -> "Επανάληψη κάθε " + row.repeatDays + " ημέρες"
                    },
                    color = ElectricViolet,
                    style = MaterialTheme.typography.bodySmall
                )
                if (row.note.isNotBlank()) {
                    Text(
                        row.note,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Switch(
                checked = row.enabled,
                onCheckedChange = onToggle
            )
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
private fun ReminderEditorDialog(
    current: ReminderEntry?,
    notificationsAllowed: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var peptide by remember(current) {
        mutableStateOf(current?.peptide ?: peptideCatalog.firstOrNull()?.name.orEmpty())
    }
    var peptideMenu by remember { mutableStateOf(false) }
    var note by remember(current) { mutableStateOf(current?.note.orEmpty()) }
    var scheduledAt by remember(current) {
        mutableStateOf(
            current?.scheduledAt?.takeIf { it > System.currentTimeMillis() }
                ?: (System.currentTimeMillis() + 60L * 60L * 1000L)
        )
    }
    var repeatDays by remember(current) { mutableStateOf(current?.repeatDays ?: 0) }
    var enabled by remember(current) { mutableStateOf(current?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(if (current == null) "Νέα υπενθύμιση" else "Επεξεργασία υπενθύμισης")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    "Καταχώρησε μόνο το δικό σου πλάνο. Δεν παρέχεται πρόταση δοσολογίας ή συχνότητας.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Box {
                    OutlinedButton(
                        onClick = { peptideMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            peptide.ifBlank { "Επίλεξε πεπτίδιο" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showReminderDatePicker(context, scheduledAt) { scheduledAt = it }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            DateFormat.getDateInstance(DateFormat.SHORT).format(Date(scheduledAt)),
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            showReminderTimePicker(context, scheduledAt) { scheduledAt = it }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(scheduledAt)),
                            maxLines = 1
                        )
                    }
                }

                Text("Επανάληψη", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to "Μία φορά",
                        1 to "Κάθε μέρα",
                        7 to "Κάθε 7ημ."
                    ).forEach { (days, label) ->
                        FilterChip(
                            selected = repeatDays == days,
                            onClick = { repeatDays = days },
                            label = { Text(label, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = premiumFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Σημείωση (προαιρετικά)") },
                    colors = premiumTextFieldColors()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Ενεργή υπενθύμιση", fontWeight = FontWeight.Bold)
                        if (!notificationsAllowed) {
                            Text(
                                "Χρειάζεται άδεια Android notifications.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Switch(
                        checked = enabled && notificationsAllowed,
                        onCheckedChange = { enabled = it },
                        enabled = notificationsAllowed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        peptide.trim(),
                        note.trim(),
                        scheduledAt,
                        repeatDays,
                        enabled
                    )
                },
                enabled = peptide.isNotBlank() && scheduledAt > System.currentTimeMillis(),
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

private fun showReminderDatePicker(
    context: Context,
    timestamp: Long,
    onPicked: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val updated = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
            onPicked(updated.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showReminderTimePicker(
    context: Context,
    timestamp: Long,
    onPicked: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    TimePickerDialog(
        context,
        { _, hour, minute ->
            val updated = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onPicked(updated.timeInMillis)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}
