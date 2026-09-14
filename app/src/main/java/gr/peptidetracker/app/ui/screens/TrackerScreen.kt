package gr.peptidetracker.app.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.InventoryEntry
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.ProgressEntry
import gr.peptidetracker.app.data.TrackerEntry
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.NeonRose
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage
import gr.peptidetracker.app.ui.GlassSurfaceStrong
import gr.peptidetracker.app.ui.TextPrimary
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumFilterChipColors
import gr.peptidetracker.app.ui.premiumTextButtonColors
import gr.peptidetracker.app.ui.premiumTextFieldColors
import java.text.DateFormat
import java.util.Date
import kotlin.math.max
import kotlin.math.min

@Composable
fun TrackerScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    onSettings: () -> Unit
) {
    var section by remember { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 18.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PremiumTopBar(
                title = "Καταγραφές",
                subtitle = "Καταγραφές, απόθεμα και πρόοδος σε ένα μέρος.",
                onSettings = onSettings
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                TrackerTab(
                    text = "Logs",
                    selected = section == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { section = 0 }
                )
                TrackerTab(
                    text = "Απόθεμα",
                    selected = section == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { section = 1 }
                )
                TrackerTab(
                    text = "Πρόοδος",
                    selected = section == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { section = 2 }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (section) {
            0 -> LogsSection(store = store, imageIndex = imageIndex)
            1 -> InventorySection(store = store, imageIndex = imageIndex)
            else -> ProgressSection(store = store)
        }
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
    store: LocalStore,
    imageIndex: Map<String, String>
) {
    var rows by remember { mutableStateOf(store.entries()) }
    var showAdd by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = "Καταγραφές",
                subtitle = rows.size.toString() + " αποθηκευμένες εγγραφές",
                icon = Icons.AutoMirrored.Rounded.EventNote,
                accent = ElectricBlue,
                buttonText = "Νέα",
                onAdd = { showAdd = true }
            )
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = "Δεν υπάρχουν καταγραφές",
                    text = "Οι εγγραφές που προσθέτεις θα εμφανίζονται εδώ.",
                    icon = Icons.AutoMirrored.Rounded.EventNote
                )
            }
        } else {
            items(rows, key = { it.id }) { row ->
                LogCard(
                    row = row,
                    imageIndex = imageIndex,
                    onDelete = {
                        store.deleteEntry(row.id)
                        rows = store.entries()
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showAdd) {
        AddLogDialog(
            onDismiss = { showAdd = false },
            onSave = { peptide, amount, note ->
                store.add(peptide, amount, note)
                rows = store.entries()
                showAdd = false
            }
        )
    }
}

@Composable
private fun InventorySection(
    store: LocalStore,
    imageIndex: Map<String, String>
) {
    var rows by remember { mutableStateOf(store.inventory()) }
    var showAdd by remember { mutableStateOf(false) }

    val totalVials = rows.sumOf { it.quantity }
    val lowStock = rows.count { it.quantity <= 1 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = "Απόθεμα",
                subtitle = totalVials.toString() + " φιαλίδια · " + lowStock + " low stock",
                icon = Icons.Rounded.Inventory2,
                accent = ElectricViolet,
                buttonText = "Προσθήκη",
                onAdd = { showAdd = true }
            )
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = "Το απόθεμα είναι άδειο",
                    text = "Πρόσθεσε φιαλίδια και batch ώστε να έχεις καθαρή εικόνα.",
                    icon = Icons.Rounded.Inventory2
                )
            }
        } else {
            items(rows, key = { it.id }) { row ->
                InventoryCard(
                    row = row,
                    imageIndex = imageIndex,
                    onDelete = {
                        store.deleteInventory(row.id)
                        rows = store.inventory()
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showAdd) {
        AddInventoryDialog(
            onDismiss = { showAdd = false },
            onSave = { peptide, vial, quantity, batch ->
                runCatching {
                    store.addInventory(peptide, vial, quantity, batch)
                }.onSuccess {
                    rows = store.inventory()
                    showAdd = false
                }
            }
        )
    }
}

@Composable
private fun ProgressSection(store: LocalStore) {
    var rows by remember { mutableStateOf(store.progress()) }
    var showAdd by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHero(
                title = "Πρόοδος",
                subtitle = rows.size.toString() + " μετρήσεις",
                icon = Icons.Rounded.MonitorWeight,
                accent = ElectricCyan,
                buttonText = "Μέτρηση",
                onAdd = { showAdd = true }
            )
        }

        if (rows.size >= 2) {
            item {
                ProgressOverview(rows = rows)
            }
        }

        if (rows.isEmpty()) {
            item {
                EmptyTrackerState(
                    title = "Δεν υπάρχουν μετρήσεις",
                    text = "Πρόσθεσε βάρος ή/και μέση για να δημιουργηθεί trend.",
                    icon = Icons.Rounded.MonitorWeight
                )
            }
        } else {
            items(rows, key = { it.id }) { row ->
                ProgressCard(
                    row = row,
                    onDelete = {
                        store.deleteProgress(row.id)
                        rows = store.progress()
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showAdd) {
        AddProgressDialog(
            onDismiss = { showAdd = false },
            onSave = { weight, waist, note ->
                store.addProgress(weight, waist, note)
                rows = store.progress()
                showAdd = false
            }
        )
    }
}

@Composable
private fun SectionHero(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    buttonText: String,
    onAdd: () -> Unit
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
            Button(onClick = onAdd, colors = premiumButtonColors()) {
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
            Icon(
                icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(40.dp)
            )
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
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    row.amount,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
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
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(row.createdAt)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
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
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                    if (row.quantity <= 1) {
                        Spacer(Modifier.width(7.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonRose.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = NeonRose,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "ΧΑΜΗΛΟ",
                                    color = NeonRose,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
                Text(
                    row.quantity.toString() + " × " + row.vialMg + " mg",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    row.batch.ifBlank { "Χωρίς παρτίδα" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
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
                        newest.weight.toString() + " kg",
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

        drawPath(
            path = path,
            color = ElectricCyan,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        points.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = ((value - minValue) / range).toFloat()
            val y = size.height - (normalized * size.height * 0.78f) - size.height * 0.11f
            drawCircle(
                color = ElectricBlue,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun ProgressCard(
    row: ProgressEntry,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    row.weight.toString() + " kg",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (row.waist != null) {
                    Text(
                        "Μέση: " + row.waist + " cm",
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
private fun AddLogDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var peptide by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Νέα καταγραφή") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = peptide,
                    onValueChange = { peptide = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Πεπτίδιο") },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ποσότητα / μονάδα") },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Σημείωση") },
                    colors = premiumTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(peptide.trim(), amount.trim(), note.trim()) },
                enabled = peptide.isNotBlank() && amount.isNotBlank(),
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
private fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String) -> Unit
) {
    var peptide by remember { mutableStateOf("") }
    var vial by remember { mutableStateOf("10") }
    var quantity by remember { mutableStateOf("1") }
    var batch by remember { mutableStateOf("") }

    val vialValue = vial.replace(',', '.').toDoubleOrNull()
    val quantityValue = quantity.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Προσθήκη στο απόθεμα") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = peptide,
                    onValueChange = { peptide = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Πεπτίδιο") },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = vial,
                    onValueChange = { vial = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Φιαλίδιο (mg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ποσότητα") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Παρτίδα / lot") },
                    singleLine = true,
                    colors = premiumTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        peptide.trim(),
                        vialValue ?: return@Button,
                        quantityValue ?: return@Button,
                        batch.trim()
                    )
                },
                enabled = peptide.isNotBlank() &&
                    vialValue != null && vialValue > 0 &&
                    quantityValue != null && quantityValue > 0,
                colors = premiumButtonColors()
            ) {
                Text("Προσθήκη")
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
private fun AddProgressDialog(
    onDismiss: () -> Unit,
    onSave: (Double, Double?, String) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val weightValue = weight.replace(',', '.').toDoubleOrNull()
    val waistValue = waist.replace(',', '.').toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurfaceStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Νέα μέτρηση") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Βάρος (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = premiumTextFieldColors()
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Μέση (cm, προαιρετικό)") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        weightValue ?: return@Button,
                        waistValue,
                        note.trim()
                    )
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
