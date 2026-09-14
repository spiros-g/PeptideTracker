package gr.peptidetracker.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

data class CalculatorPreset(
    val peptideName: String,
    val vialMg: Double? = null,
    val diluentMl: Double? = null,
    val syringeUnitsPerMl: Int = 100
)

@Composable
fun CalculatorScreen(
    imageIndex: Map<String, String>,
    preset: CalculatorPreset? = null,
    defaultSyringeUnitsPerMl: Int = 100,
    onPresetConsumed: () -> Unit = {}
) {
    var reverse by remember { mutableStateOf(false) }
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

    LaunchedEffect(preset) {
        if (preset != null) {
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
            error = ""
            onPresetConsumed()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Υπολογιστής Ανασύστασης & Δοσολογίας",
                subtitle = "Βάλε τι έχει το φιαλίδιο, πόσο διαλύτη πρόσθεσες και την ποσότητα που θέλεις να μετρήσεις."
            )
        }

        if (preset != null) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "ΠΡΟΕΠΙΛΟΓΗ",
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            preset.peptideName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (preset.vialMg != null) {
                            Text(
                                "Φιαλίδιο " + PeptideCalculator.format(preset.vialMg, 4) + " mg φορτώθηκε αυτόματα.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (preset.diluentMl != null) {
                            Text(
                                "Ανασύσταση " + PeptideCalculator.format(preset.diluentMl, 4) +
                                    " mL · U-" + (if (preset.syringeUnitsPerMl == 40) 40 else 100),
                                color = ElectricViolet,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
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
                            "ΑΝΑΣΥΣΤΑΣΗ & ΔΟΣΟΜΕΤΡΙΑ",
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            "mg · mcg · mL · U-100 / U-40",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            "Μετατρέπει την ποσότητα που εισάγεις σε mL και μονάδες σύριγγας U-100 ή U-40 — ή το αντίστροφο.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    StoreVialImage(
                        productKey = "retatrutide",
                        imageIndex = imageIndex,
                        modifier = Modifier.size(width = 82.dp, height = 150.dp),
                        contentDescription = "Φιαλίδιο πεπτιδίου"
                    )
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    text = "Ποσότητα → Μονάδες",
                    selected = !reverse,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reverse = false
                        output = emptyList()
                        error = ""
                    }
                )
                ModeChip(
                    text = "Μονάδες → Ποσότητα",
                    selected = reverse,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reverse = true
                        output = emptyList()
                        error = ""
                    }
                )
            }
        }

        item {
            CalculatorStep(
                number = "01",
                title = "1. Τι σύριγγα χρησιμοποιείς;",
                subtitle = "Διάλεξε τύπο σύριγγας. U-100 = 100 μονάδες/mL, U-40 = 40 μονάδες/mL.",
                icon = Icons.Rounded.Straighten,
                accent = ElectricBlue
            ) {
                ChoiceRow(
                    choices = listOf("U-100", "U-40"),
                    selected = "U-" + syringeUnitsPerMl,
                    onSelected = {
                        syringeUnitsPerMl = if (it == "U-40") 40 else 100
                        syringeCapacity = if (syringeUnitsPerMl == 40) 40 else 30
                        output = emptyList()
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Χωρητικότητα / μέγιστη ένδειξη",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                ChoiceRow(
                    choices = if (syringeUnitsPerMl == 40) listOf("40") else listOf("30", "50", "100"),
                    selected = syringeCapacity.toString(),
                    onSelected = { syringeCapacity = it.toInt() }
                )
            }
        }

        item {
            CalculatorStep(
                number = "02",
                title = "2. Τι περιέχει το φιαλίδιο;",
                subtitle = "Βάλε τη συνολική ποσότητα που γράφει το φιαλίδιο πριν προσθέσεις διαλύτη.",
                icon = Icons.Rounded.Science,
                accent = ElectricViolet
            ) {
                ChoiceRow(
                    choices = listOf("mg", "mcg"),
                    selected = vialUnit,
                    onSelected = {
                        vialUnit = it
                        vialAmount = if (it == "mg") "5" else "500"
                        output = emptyList()
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
                    onSelected = { vialAmount = it }
                )
                Spacer(Modifier.height(8.dp))
                DecimalField(
                    label = "Ποσότητα στο φιαλίδιο (" + vialUnit + ")",
                    value = vialAmount,
                    onValueChange = { vialAmount = it }
                )
            }
        }

        item {
            CalculatorStep(
                number = "03",
                title = "3. Πόσο διαλύτη πρόσθεσες;",
                subtitle = "Βάλε τα συνολικά mL διαλύτη που πρόσθεσες στο φιαλίδιο.",
                icon = Icons.Rounded.WaterDrop,
                accent = ElectricCyan
            ) {
                ChoiceRow(
                    choices = listOf("1", "2", "3", "5"),
                    selected = diluentMl,
                    onSelected = { diluentMl = it }
                )
                Spacer(Modifier.height(8.dp))
                DecimalField(
                    label = "Διαλύτης που πρόσθεσες (mL)",
                    value = diluentMl,
                    onValueChange = { diluentMl = it }
                )
            }
        }

        item {
            CalculatorStep(
                number = "04",
                title = if (reverse) "4. Τι δείχνει η σύριγγα;" else "4. Πόση ποσότητα θέλεις να μετρήσεις;",
                subtitle = if (reverse) {
                    "Βάλε τις μονάδες U-" + syringeUnitsPerMl + " και θα δεις σε τι ποσότητα αντιστοιχούν."
                } else {
                    "Βάλε την ποσότητα και θα δεις πόσα mL και πόσες μονάδες U-" + syringeUnitsPerMl + " αντιστοιχούν."
                },
                icon = Icons.Rounded.InvertColors,
                accent = Color(0xFFFFB36B)
            ) {
                if (reverse) {
                    DecimalField(
                        label = "Μονάδες που δείχνει η σύριγγα (U-" + syringeUnitsPerMl + ")",
                        value = syringeUnits,
                        onValueChange = { syringeUnits = it }
                    )
                } else {
                    ChoiceRow(
                        choices = listOf("mg", "mcg"),
                        selected = targetUnit,
                        onSelected = {
                            targetUnit = it
                            targetAmount = if (it == "mg") "0.1" else "100"
                            output = emptyList()
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
                        onSelected = { targetAmount = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    DecimalField(
                        label = "Ποσότητα που θέλεις να μετρήσεις (" + targetUnit + ")",
                        value = targetAmount,
                        onValueChange = { targetAmount = it }
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
                            val result = PeptideCalculator.dose(
                                vialMg = vialInMg,
                                diluentMl = water,
                                target = targetAmount.toDouble(),
                                targetIsMg = targetUnit == "mg",
                                syringeUnitsPerMl = syringeUnitsPerMl
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
                            listOf(
                                PeptideCalculator.format(result.amountMg, 4) + " mg",
                                PeptideCalculator.format(result.amountMcg, 2) + " mcg",
                                PeptideCalculator.format(result.volumeMl, 4) + " mL",
                                PeptideCalculator.format(concentration.mcgPerUnit, 3) + " mcg/U"
                            )
                        }
                        error = ""
                    }.onFailure {
                        output = emptyList()
                        error = "Συμπλήρωσε έγκυρες θετικές αριθμητικές τιμές."
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
                Text("Υπολογισμός", fontWeight = FontWeight.ExtraBold)
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

        item {
            Text(
                "Ο υπολογιστής κάνει μόνο μαθηματική μετατροπή από τις τιμές που βάζεις. Δεν επιλέγει για εσένα ποια ποσότητα πρέπει να χρησιμοποιήσεις.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
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
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
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
                label = { Text(choice) },
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
                "ΑΠΟΤΕΛΕΣΜΑ",
                color = ElectricCyan,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (!reverse) {
                ResultLine(
                    label = "Τράβηξε μέχρι τις μονάδες U-" + syringeUnitsPerMl,
                    value = output[0],
                    emphasize = true
                )
                ResultLine(
                    label = "Αντίστοιχος όγκος",
                    value = output[1]
                )
                ResultLine(
                    label = "Συγκέντρωση μετά την ανάμιξη",
                    value = output[2]
                )
                ResultLine(
                    label = "Ποσότητα ανά 1 μονάδα U-" + syringeUnitsPerMl,
                    value = output[3]
                )
            } else {
                ResultLine(
                    label = "Ποσότητα",
                    value = output[0],
                    emphasize = true
                )
                ResultLine(
                    label = "Ίδια ποσότητα σε mcg",
                    value = output[1]
                )
                ResultLine(
                    label = "Αντίστοιχος όγκος",
                    value = output[2]
                )
                ResultLine(
                    label = "Ποσότητα ανά 1 μονάδα U-" + syringeUnitsPerMl,
                    value = output[3]
                )
            }

            if (!reverse) {
                Spacer(Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = {
                        (units / capacity.toDouble())
                            .coerceIn(0.0, 1.0)
                            .toFloat()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = ElectricCyan,
                    trackColor = Color.White.copy(alpha = 0.09f)
                )
                Text(
                    "Σύριγγα U-" + syringeUnitsPerMl + " · μέγιστη ένδειξη " + capacity + " U",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (units > capacity) {
                    Text(
                        "Το αποτέλεσμα υπερβαίνει την επιλεγμένη χωρητικότητα.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
