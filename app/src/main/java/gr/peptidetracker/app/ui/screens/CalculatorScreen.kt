package gr.peptidetracker.app.ui.screens

import gr.peptidetracker.app.i18n.t

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.SavedCalculationEntry
import gr.peptidetracker.app.domain.PeptideCalculator
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumFilterChipColors
import gr.peptidetracker.app.ui.premiumTextFieldColors
import java.text.DateFormat
import java.util.Date

data class CalculatorPreset(
    val peptideName: String,
    val vialMg: Double? = null,
    val diluentMl: Double? = null,
    val syringeUnitsPerMl: Int = 100
)

private data class CalculatedDose(
    val vialMg: Double,
    val diluentMl: Double,
    val amountValue: Double,
    val amountUnit: String,
    val syringeUnits: Double
)

@Composable
fun CalculatorScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    preset: CalculatorPreset? = null,
    defaultSyringeUnitsPerMl: Int = 100,
    onPresetConsumed: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {
    var reverse by remember { mutableStateOf(false) }
    var peptideName by remember { mutableStateOf("") }
    var peptideMenu by remember { mutableStateOf(false) }
    var syringeUnitsPerMl by remember(defaultSyringeUnitsPerMl) {
        mutableIntStateOf(if (defaultSyringeUnitsPerMl == 40) 40 else 100)
    }
    var syringeCapacity by remember(defaultSyringeUnitsPerMl) {
        mutableIntStateOf(if (defaultSyringeUnitsPerMl == 40) 40 else 30)
    }
    var vialAmount by remember { mutableStateOf("5") }
    var vialUnit by remember { mutableStateOf("mg") }
    var diluentMl by remember { mutableStateOf("1") }
    var targetAmount by remember { mutableStateOf("0.1") }
    var targetUnit by remember { mutableStateOf("mg") }
    var syringeUnits by remember { mutableStateOf("5") }
    var output by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }
    var lastCalculation by remember { mutableStateOf<CalculatedDose?>(null) }
    var savedCalculations by remember { mutableStateOf(store.savedCalculations()) }

    LaunchedEffect(preset) {
        if (preset != null) {
            peptideName = preset.peptideName
            preset.vialMg?.let {
                vialUnit = "mg"
                vialAmount = PeptideCalculator.format(it, 4)
            }
            preset.diluentMl?.let {
                diluentMl = PeptideCalculator.format(it, 4)
            }
            syringeUnitsPerMl = if (preset.syringeUnitsPerMl == 40) 40 else 100
            syringeCapacity = if (syringeUnitsPerMl == 40) 40 else 30
            output = emptyList()
            lastCalculation = null
            error = ""
            infoMessage = ""
            onPresetConsumed()
        }
    }

    fun clearResult() {
        output = emptyList()
        lastCalculation = null
        error = ""
        infoMessage = ""
    }

    fun loadSaved(row: SavedCalculationEntry) {
        peptideName = row.peptide
        vialUnit = "mg"
        vialAmount = PeptideCalculator.format(row.vialMg, 4)
        diluentMl = PeptideCalculator.format(row.diluentMl, 4)
        targetAmount = PeptideCalculator.format(row.targetAmount, 4)
        targetUnit = row.targetUnit
        syringeUnitsPerMl = row.syringeUnitsPerMl
        syringeCapacity = row.syringeCapacity
        reverse = false
        clearResult()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = t("Υπολογιστής Ανασύστασης & Δοσομετρίας"),
                subtitle = t("Βάλε τι έχει το φιαλίδιο, πόσο διαλύτη πρόσθεσες και την ποσότητα που θέλεις να μετατρέψεις.")
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 190.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ElectricBlue.copy(alpha = 0.18f),
                                    ElectricViolet.copy(alpha = 0.11f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            t("ΑΝΑΣΥΣΤΑΣΗ & ΔΟΣΟΜΕΤΡΙΑ"),
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2
                        )

                        Text(
                            "mg · mcg · mL · U-100 / U-40",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2
                        )

                        Text(
                            t("Καθαρή μαθηματική μετατροπή των τιμών που εισάγεις. Δεν επιλέγει ποσότητα ή πρόγραμμα χρήσης."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    StoreVialImage(
                        productKey = peptideName.ifBlank { "retatrutide" },
                        imageIndex = imageIndex,
                        modifier = Modifier.size(width = 82.dp, height = 150.dp),
                        contentDescription = t("Φιαλίδιο πεπτιδίου")
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t("Πεπτίδιο"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        t("Προαιρετικό για τον υπολογισμό. Χρειάζεται μόνο αν θέλεις να αποθηκεύσεις το αποτέλεσμα στο ημερολόγιο."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Box {
                        OutlinedButton(
                            onClick = { peptideMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                peptideName.ifBlank { t("Επίλεξε πεπτίδιο") },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = peptideMenu,
                            onDismissRequest = { peptideMenu = false }
                        ) {
                            store.peptideNames().forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        peptideName = name
                                        peptideMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    text = t("Ποσότητα → Μονάδες"),
                    selected = !reverse,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reverse = false
                        clearResult()
                    }
                )
                ModeChip(
                    text = t("Μονάδες → Ποσότητα"),
                    selected = reverse,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reverse = true
                        clearResult()
                    }
                )
            }
        }

        item {
            CalculatorStep(
                number = "01",
                title = t("Τι σύριγγα χρησιμοποιείς;"),
                subtitle = t("U-100 = 100 μονάδες/mL, U-40 = 40 μονάδες/mL."),
                icon = Icons.Rounded.Straighten,
                accent = ElectricBlue
            ) {
                ChoiceRow(
                    choices = listOf("U-100", "U-40"),
                    selected = "U-" + syringeUnitsPerMl,
                    onSelected = {
                        syringeUnitsPerMl = if (it == "U-40") 40 else 100
                        syringeCapacity = if (syringeUnitsPerMl == 40) 40 else 30
                        clearResult()
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    t("Χωρητικότητα / μέγιστη ένδειξη"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                ChoiceRow(
                    choices = if (syringeUnitsPerMl == 40) listOf("40") else listOf("30", "50", "100"),
                    selected = syringeCapacity.toString(),
                    onSelected = {
                        syringeCapacity = it.toInt()
                        clearResult()
                    }
                )
            }
        }

        item {
            CalculatorStep(
                number = "02",
                title = t("Τι περιέχει το φιαλίδιο;"),
                subtitle = t("Βάλε τη συνολική ποσότητα που γράφει το φιαλίδιο πριν προσθέσεις διαλύτη."),
                icon = Icons.Rounded.Science,
                accent = ElectricViolet
            ) {
                ChoiceRow(
                    choices = listOf("mg", "mcg"),
                    selected = vialUnit,
                    onSelected = {
                        vialUnit = it
                        vialAmount = if (it == "mg") "5" else "500"
                        clearResult()
                    }
                )
                Spacer(Modifier.height(8.dp))
                ChoiceRow(
                    choices = if (vialUnit == "mg") {
                        listOf("5", "10", "20", "30")
                    } else {
                        listOf("100", "250", "500", "1000")
                    },
                    selected = vialAmount,
                    onSelected = {
                        vialAmount = it
                        clearResult()
                    }
                )
                Spacer(Modifier.height(8.dp))
                DecimalField(
                    label = t("Ποσότητα στο φιαλίδιο (") + vialUnit + ")",
                    value = vialAmount,
                    onValueChange = {
                        vialAmount = it
                        clearResult()
                    }
                )
            }
        }

        item {
            CalculatorStep(
                number = "03",
                title = t("Πόσο διαλύτη πρόσθεσες;"),
                subtitle = t("Βάλε τα συνολικά mL διαλύτη που πρόσθεσες στο φιαλίδιο."),
                icon = Icons.Rounded.WaterDrop,
                accent = ElectricCyan
            ) {
                ChoiceRow(
                    choices = listOf("1", "2", "3", "5"),
                    selected = diluentMl,
                    onSelected = {
                        diluentMl = it
                        clearResult()
                    }
                )
                Spacer(Modifier.height(8.dp))
                DecimalField(
                    label = t("Διαλύτης που πρόσθεσες (mL)"),
                    value = diluentMl,
                    onValueChange = {
                        diluentMl = it
                        clearResult()
                    }
                )
            }
        }

        item {
            CalculatorStep(
                number = "04",
                title = if (reverse) t("Τι δείχνει η σύριγγα;") else t("Ποια ποσότητα θέλεις να μετατρέψεις;"),
                subtitle = if (reverse) {
                    t("Βάλε τις μονάδες U-") + syringeUnitsPerMl + t(" και θα δεις σε τι ποσότητα αντιστοιχούν.")
                } else {
                    t("Βάλε την ποσότητα και θα δεις πόσα mL και πόσες μονάδες U-") + syringeUnitsPerMl + t(" αντιστοιχούν.")
                },
                icon = Icons.Rounded.InvertColors,
                accent = Color(0xFFFFB36B)
            ) {
                if (reverse) {
                    DecimalField(
                        label = t("Μονάδες που δείχνει η σύριγγα (U-") + syringeUnitsPerMl + ")",
                        value = syringeUnits,
                        onValueChange = {
                            syringeUnits = it
                            clearResult()
                        }
                    )
                } else {
                    ChoiceRow(
                        choices = listOf("mg", "mcg"),
                        selected = targetUnit,
                        onSelected = {
                            targetUnit = it
                            targetAmount = if (it == "mg") "0.1" else "100"
                            clearResult()
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    ChoiceRow(
                        choices = if (targetUnit == "mg") {
                            listOf("0.1", "0.25", "0.5", "1")
                        } else {
                            listOf("50", "100", "250", "500")
                        },
                        selected = targetAmount,
                        onSelected = {
                            targetAmount = it
                            clearResult()
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    DecimalField(
                        label = t("Ποσότητα (") + targetUnit + ")",
                        value = targetAmount,
                        onValueChange = {
                            targetAmount = it
                            clearResult()
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    runCatching {
                        val vialInput = vialAmount.toDouble()
                        val vialInMg = if (vialUnit == "mg") vialInput else vialInput / 1000.0
                        val water = diluentMl.toDouble()
                        val concentration = PeptideCalculator.concentration(
                            vialAmount = vialInput,
                            vialIsMcg = vialUnit == "mcg",
                            diluentMl = water,
                            syringeUnitsPerMl = syringeUnitsPerMl
                        )

                        output = if (!reverse) {
                            val requested = targetAmount.toDouble()
                            val result = PeptideCalculator.dose(
                                vialMg = vialInMg,
                                diluentMl = water,
                                target = requested,
                                targetIsMg = targetUnit == "mg",
                                syringeUnitsPerMl = syringeUnitsPerMl
                            )
                            lastCalculation = CalculatedDose(
                                vialMg = vialInMg,
                                diluentMl = water,
                                amountValue = requested,
                                amountUnit = targetUnit,
                                syringeUnits = result.syringeUnits
                            )
                            listOf(
                                PeptideCalculator.format(result.syringeUnits, 2) + " U",
                                PeptideCalculator.format(result.volumeMl, 4) + " mL",
                                PeptideCalculator.format(concentration.mgPerMl, 4) + " mg/mL",
                                PeptideCalculator.format(concentration.mcgPerUnit, 3) + " mcg/U"
                            )
                        } else {
                            val result = PeptideCalculator.reverse(
                                vialMg = vialInMg,
                                diluentMl = water,
                                units = syringeUnits.toDouble(),
                                syringeUnitsPerMl = syringeUnitsPerMl
                            )
                            lastCalculation = CalculatedDose(
                                vialMg = vialInMg,
                                diluentMl = water,
                                amountValue = result.amountMg,
                                amountUnit = "mg",
                                syringeUnits = syringeUnits.toDouble()
                            )
                            listOf(
                                PeptideCalculator.format(result.amountMg, 4) + " mg",
                                PeptideCalculator.format(result.amountMcg, 2) + " mcg",
                                PeptideCalculator.format(result.volumeMl, 4) + " mL",
                                PeptideCalculator.format(concentration.mcgPerUnit, 3) + " mcg/U"
                            )
                        }
                        error = ""
                        infoMessage = ""
                    }.onFailure {
                        output = emptyList()
                        lastCalculation = null
                        error = t("Συμπλήρωσε έγκυρες θετικές αριθμητικές τιμές.")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = premiumButtonColors()
            ) {
                Icon(Icons.Rounded.Calculate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(t("Υπολογισμός"), fontWeight = FontWeight.ExtraBold)
            }
        }

        if (error.isNotEmpty()) {
            item {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = output.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (output.isNotEmpty()) {
                    ResultCard(
                        output = output,
                        capacity = syringeCapacity,
                        reverse = reverse,
                        syringeUnitsPerMl = syringeUnitsPerMl
                    )
                }
            }
        }

        if (lastCalculation != null) {
            item {
                val calculation = lastCalculation!!
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            t("Ενέργειες αποτελέσματος"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    store.addSavedCalculation(
                                        peptide = peptideName,
                                        vialMg = calculation.vialMg,
                                        diluentMl = calculation.diluentMl,
                                        targetAmount = calculation.amountValue,
                                        targetUnit = calculation.amountUnit,
                                        syringeUnitsPerMl = syringeUnitsPerMl,
                                        syringeCapacity = syringeCapacity,
                                        syringeUnits = calculation.syringeUnits
                                    )
                                    savedCalculations = store.savedCalculations()
                                    infoMessage = t("Ο υπολογισμός αποθηκεύτηκε.")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(t("Αποθήκευση"))
                            }

                            Button(
                                onClick = {
                                    if (peptideName.isNotBlank()) {
                                        store.addEntry(
                                            peptide = peptideName,
                                            amountValue = calculation.amountValue,
                                            unit = calculation.amountUnit,
                                            note = t("Από υπολογιστή ανασύστασης · ") +
                                                PeptideCalculator.format(calculation.syringeUnits, 2) + " U",
                                            site = "",
                                            createdAt = System.currentTimeMillis()
                                        )
                                        infoMessage = t("Προστέθηκε στο ημερολόγιο.")
                                        onOpenHistory()
                                    }
                                },
                                enabled = peptideName.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                colors = premiumButtonColors()
                            ) {
                                Icon(Icons.Rounded.EventNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(t("Ημερολόγιο"))
                            }
                        }
                        if (peptideName.isBlank()) {
                            Text(
                                t("Επίλεξε πεπτίδιο για να ενεργοποιηθεί η καταχώρηση στο ημερολόγιο."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        if (infoMessage.isNotBlank()) {
            item {
                Text(
                    infoMessage,
                    color = ElectricCyan,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        if (savedCalculations.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = ElectricViolet)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        t("Αποθηκευμένοι υπολογισμοί"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        savedCalculations.size.toString(),
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(savedCalculations.take(5), key = { it.id }) { row ->
                SavedCalculationCard(
                    row = row,
                    onLoad = { loadSaved(row) },
                    onDelete = {
                        store.deleteSavedCalculation(row.id)
                        savedCalculations = store.savedCalculations()
                    }
                )
            }
        }

        item {
            Text(
                t("Ο υπολογιστής κάνει μόνο μαθηματική μετατροπή από τις τιμές που βάζεις. Δεν επιλέγει για εσένα ποια ποσότητα ή συχνότητα πρέπει να χρησιμοποιήσεις."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SavedCalculationCard(
    row: SavedCalculationEntry,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onLoad) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    row.peptide.ifBlank { t("Χωρίς επιλεγμένο πεπτίδιο") },
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    PeptideCalculator.format(row.vialMg, 4) + " mg / " +
                        PeptideCalculator.format(row.diluentMl, 4) + " mL · " +
                        PeptideCalculator.format(row.targetAmount, 4) + " " + row.targetUnit,
                    color = ElectricCyan,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    PeptideCalculator.format(row.syringeUnits, 2) + " U · U-" +
                        row.syringeUnitsPerMl + " · " +
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(Date(row.createdAt)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            OutlinedButton(onClick = onLoad) {
                Text(t("Φόρτωση"))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = t("Διαγραφή"))
            }
        }
    }
}

@Composable
private fun ModeChip(
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
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 2
            )
        },
        modifier = modifier,
        colors = premiumFilterChipColors()
    )
}

@Composable
private fun CalculatorStep(
    number: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    content: @Composable () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        number + "  " + title,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun ChoiceRow(
    choices: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        choices.forEach { choice ->
            FilterChip(
                selected = selected == choice,
                onClick = { onSelected(choice) },
                label = { Text(choice, maxLines = 1) },
                modifier = Modifier.weight(1f),
                colors = premiumFilterChipColors()
            )
        }
    }
}

@Composable
private fun DecimalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.replace(',', '.')) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = premiumTextFieldColors()
    )
}

@Composable
private fun ResultLine(
    label: String,
    value: String,
    emphasize: Boolean = false
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
            style = if (emphasize) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ResultCard(
    output: List<String>,
    capacity: Int,
    reverse: Boolean,
    syringeUnitsPerMl: Int
) {
    val units = if (!reverse) {
        output.firstOrNull()?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
    } else {
        0.0
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            ElectricBlue.copy(alpha = 0.20f),
                            ElectricCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                t("ΑΠΟΤΕΛΕΣΜΑ"),
                color = ElectricCyan,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (!reverse) {
                ResultLine(
                    label = t("Ένδειξη σύριγγας U-") + syringeUnitsPerMl,
                    value = output[0],
                    emphasize = true
                )
                ResultLine(
                    label = t("Αντίστοιχος όγκος"),
                    value = output[1]
                )
                ResultLine(
                    label = t("Συγκέντρωση μετά την ανάμιξη"),
                    value = output[2]
                )
                ResultLine(
                    label = t("Ποσότητα ανά 1 μονάδα U-") + syringeUnitsPerMl,
                    value = output[3]
                )
            } else {
                ResultLine(
                    label = t("Ποσότητα"),
                    value = output[0],
                    emphasize = true
                )
                ResultLine(
                    label = t("Ίδια ποσότητα σε mcg"),
                    value = output[1]
                )
                ResultLine(
                    label = t("Αντίστοιχος όγκος"),
                    value = output[2]
                )
                ResultLine(
                    label = t("Ποσότητα ανά 1 μονάδα U-") + syringeUnitsPerMl,
                    value = output[3]
                )
            }

            if (!reverse) {
                Spacer(Modifier.height(4.dp))
                SyringeGauge(
                    units = units,
                    capacity = capacity
                )
                Text(
                    t("Σύριγγα U-") + syringeUnitsPerMl + t(" · μέγιστη ένδειξη ") + capacity + " U",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (units > capacity) {
                    Text(
                        t("Το αποτέλεσμα υπερβαίνει την επιλεγμένη χωρητικότητα."),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SyringeGauge(
    units: Double,
    capacity: Int
) {
    val fraction = (units / capacity.toDouble()).coerceIn(0.0, 1.0).toFloat()
    val outline = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val track = Color.White.copy(alpha = 0.06f)
    val fill = ElectricCyan.copy(alpha = 0.28f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        val left = 8.dp.toPx()
        val right = size.width - 28.dp.toPx()
        val top = 10.dp.toPx()
        val barrelHeight = 30.dp.toPx()
        val barrelWidth = right - left

        drawRoundRect(
            color = track,
            topLeft = Offset(left, top),
            size = Size(barrelWidth, barrelHeight),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )

        drawRoundRect(
            color = fill,
            topLeft = Offset(left, top),
            size = Size(barrelWidth * fraction, barrelHeight),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )

        drawRoundRect(
            color = outline,
            topLeft = Offset(left, top),
            size = Size(barrelWidth, barrelHeight),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        for (i in 0..10) {
            val x = left + barrelWidth * (i / 10f)
            val tickHeight = if (i % 5 == 0) 13.dp.toPx() else 8.dp.toPx()
            drawLine(
                color = outline,
                start = Offset(x, top + barrelHeight),
                end = Offset(x, top + barrelHeight + tickHeight),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawLine(
            color = outline,
            start = Offset(right, top + barrelHeight / 2f),
            end = Offset(size.width - 5.dp.toPx(), top + barrelHeight / 2f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
