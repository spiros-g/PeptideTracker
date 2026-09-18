package gr.peptidetracker.app.ui.screens

import gr.peptidetracker.app.i18n.t

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.GlassSurfaceStrong
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage
import gr.peptidetracker.app.ui.TextPrimary
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumTextButtonColors
import gr.peptidetracker.app.ui.premiumTextFieldColors
import gr.peptidetracker.app.ui.components.PeptidePickerDialog
import gr.peptidetracker.app.ui.components.SelectionPickerDialog
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RemindersScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val peptideNames = store.peptideNames()
    var rows by remember { mutableStateOf(store.reminders()) }
    var editing by remember { mutableStateOf<ReminderEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var openEditorAfterPermission by remember { mutableStateOf(false) }
    var pendingEnableId by remember { mutableStateOf<Long?>(null) }
    var pendingDelete by remember { mutableStateOf<ReminderEntry?>(null) }

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
            message = t("Οι ειδοποιήσεις ενεργοποιήθηκαν.")
            pendingEnableId?.let { id ->
                store.setReminderEnabled(id, true)?.let {
                    ReminderScheduler.schedule(context, it)
                }
            }
        } else {
            message = t("Χωρίς άδεια ειδοποιήσεων οι υπενθυμίσεις αποθηκεύονται αλλά παραμένουν ανενεργές.")
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
                title = t("Πλάνο & Υπενθυμίσεις"),
                subtitle = t("Όρισε δικές σου ημερομηνίες και ώρες. Οι ειδοποιήσεις μπορεί να καθυστερήσουν ελαφρώς από το Android για εξοικονόμηση ενέργειας."),
                onBack = onBack
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                enabledCount.toString() + t(" ενεργές"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                if (nextReminder == null) {
                                    t("Δεν υπάρχει επόμενη ενεργή υπενθύμιση.")
                                } else {
                                    t("Επόμενη: ") + DateFormat.getDateTimeInstance(
                                        DateFormat.SHORT,
                                        DateFormat.SHORT
                                    ).format(Date(nextReminder.scheduledAt))
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = premiumButtonColors()
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(t("Νέα"))
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
                            Text(t("Οι ειδοποιήσεις είναι κλειστές"), fontWeight = FontWeight.ExtraBold)
                            Text(
                                t("Οι υπενθυμίσεις μπορούν να αποθηκευτούν, αλλά δεν θα εμφανιστούν ως Android notifications."),
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
                            Text(t("Άδεια"))
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
                        Text(t("Δεν έχεις υπενθυμίσεις"), fontWeight = FontWeight.ExtraBold)
                        Text(
                            t("Πρόσθεσε ημερομηνία και ώρα για κάτι που έχεις ήδη αποφασίσει να καταγράψεις. Δεν δημιουργούνται αυτόματα πρωτόκολλα ή δοσολογίες."),
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
                        pendingDelete = row
                    }
                )
            }
        }

        item {
            Text(
                t("Οι υπενθυμίσεις είναι προσωπικές καταχωρήσεις χρόνου. Η εφαρμογή δεν επιλέγει πεπτίδιο, ποσότητα ή συχνότητα για εσένα και δεν εγγυάται εκτέλεση στο ακριβές δευτερόλεπτο."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    pendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = GlassSurfaceStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text(t("Διαγραφή υπενθύμισης;")) },
            text = { Text(t("Η υπενθύμιση θα διαγραφεί οριστικά.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        ReminderScheduler.cancel(context, reminder.id)
                        store.deleteReminder(reminder.id)
                        pendingDelete = null
                        message = t("Η υπενθύμιση διαγράφηκε.")
                        reload()
                    }
                ) {
                    Text(t("Διαγραφή"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDelete = null },
                    colors = premiumTextButtonColors()
                ) {
                    Text(t("Άκυρο"))
                }
            }
        )
    }

    if (showEditor) {
        ReminderEditorDialog(
            current = editing,
            peptideNames = peptideNames,
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
                    message = t("Η υπενθύμιση αποθηκεύτηκε ανενεργή μέχρι να επιτρέψεις notifications.")
                } else {
                    message = t("Η υπενθύμιση αποθηκεύτηκε.")
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        reminderRepeatLabel(row.repeatDays),
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
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEdit,
                    colors = premiumTextButtonColors()
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(t("Επεξεργασία"))
                }
                TextButton(
                    onClick = onDelete,
                    colors = premiumTextButtonColors()
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(t("Διαγραφή"))
                }
            }
        }
    }
}

@Composable
private fun ReminderEditorDialog(
    current: ReminderEntry?,
    peptideNames: List<String>,
    notificationsAllowed: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var peptide by remember(current) {
        mutableStateOf(current?.peptide ?: peptideNames.firstOrNull().orEmpty())
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
    var repeatMenu by remember { mutableStateOf(false) }
    var enabled by remember(current) { mutableStateOf(current?.enabled ?: true) }
    val repeatOptions = remember(current) {
        (listOf(0, 1, 2, 3, 4, 5, 7, 10, 14, 21, 28, 30) +
            listOfNotNull(current?.repeatDays?.takeIf { it > 0 }))
            .distinct()
            .sorted()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(if (current == null) t("Νέα υπενθύμιση") else t("Επεξεργασία υπενθύμισης"))
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text(
                    t("Καταχώρησε μόνο το δικό σου πλάνο. Δεν παρέχεται πρόταση δοσολογίας ή συχνότητας."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

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

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showReminderDatePicker(context, scheduledAt) { scheduledAt = it }
                        },
                        modifier = Modifier.weight(1.18f),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date(scheduledAt)),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            showReminderTimePicker(context, scheduledAt) { scheduledAt = it }
                        },
                        modifier = Modifier.weight(0.82f),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(Date(scheduledAt)),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }

                Text(t("Επανάληψη"), fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { repeatMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        reminderRepeatLabel(repeatDays),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Σημείωση (προαιρετικά)")) },
                    colors = premiumTextFieldColors()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(t("Ενεργή υπενθύμιση"), fontWeight = FontWeight.Bold)
                        if (!notificationsAllowed) {
                            Text(
                                t("Χρειάζεται άδεια Android notifications."),
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

    if (repeatMenu) {
        val repeatLabels = repeatOptions.map(::reminderRepeatLabel)
        SelectionPickerDialog(
            title = t("Επανάληψη"),
            selectedValue = reminderRepeatLabel(repeatDays),
            options = repeatLabels,
            onDismiss = { repeatMenu = false },
            onSelect = { selected ->
                val index = repeatLabels.indexOf(selected)
                if (index >= 0) {
                    repeatDays = repeatOptions[index]
                }
                repeatMenu = false
            }
        )
    }
}

private fun reminderRepeatLabel(days: Int): String = when (days) {
    0 -> t("Μία φορά")
    1 -> t("Κάθε ημέρα")
    else -> t("Κάθε $days ημέρες")
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
