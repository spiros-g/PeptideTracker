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

@Composable
fun CalculatorScreen(
    imageIndex: Map<String, String>
) {
    var reverse by remember { mutableStateOf(false) }
    var syringeCapacity by remember { mutableIntStateOf(30) }
    var vialAmount by remember { mutableStateOf("5") }
    var vialUnit by remember { mutableStateOf("mg") }
    var diluentMl by remember { mutableStateOf("1") }
    var targetAmount by remember { mutableStateOf("0.1") }
    var targetUnit by remember { mutableStateOf("mg") }
    var syringeUnits by remember { mutableStateOf("5") }
    var output by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Υπολογιστής Ανασύστασης & Δόσης",
                subtitle = "Ανασύσταση, συγκέντρωση, όγκος και μονάδες σύριγγας."
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier
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
                        .padding(18.dp)
                ) {
                    Column(
                        Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(0.68f)
                    ) {
                        Text(
                            "ΑΝΑΣΥΣΤΑΣΗ & ΥΠΟΛΟΓΙΣΜΟΣ ΔΟΣΗΣ",
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "mg · mcg · mL · U-100",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "Υπολογισμός από ποσότητα σε μονάδες U-100 και αντίστροφα.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    StoreVialImage(
                        productKey = "retatrutide",
                        imageIndex = imageIndex,
                        modifier = Modifier
                            .size(width = 88.dp, height = 122.dp)
                            .align(Alignment.CenterEnd)
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
                title = "Σύριγγα U-100",
                subtitle = "Επιλογή συνολικής χωρητικότητας σύριγγας",
                icon = Icons.Rounded.Straighten,
                accent = ElectricBlue
            ) {
                ChoiceRow(
                    choices = listOf("30", "50", "100"),
                    selected = syringeCapacity.toString(),
                    onSelected = { syringeCapacity = it.toInt() }
                )
            }
        }

        item {
            CalculatorStep(
                number = "02",
                title = "Ποσότητα φιαλιδίου",
                subtitle = "Συνολική ποσότητα πεπτιδίου πριν την ανασύσταση",
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
                    label = "Ποσότητα φιαλιδίου (" + vialUnit + ")",
                    value = vialAmount,
                    onValueChange = { vialAmount = it }
                )
            }
        }

        item {
            CalculatorStep(
                number = "03",
                title = "Όγκος ανασύστασης",
                subtitle = "Συνολικός όγκος διαλύτη που προστίθεται στο φιαλίδιο",
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
                    label = "Όγκος ανασύστασης (mL)",
                    value = diluentMl,
                    onValueChange = { diluentMl = it }
                )
            }
        }

        item {
            CalculatorStep(
                number = "04",
                title = if (reverse) "Μονάδες σύριγγας" else "Επιθυμητή ποσότητα",
                subtitle = if (reverse) "Ένδειξη σε μονάδες σύριγγας U-100" else "Ποσότητα που θέλεις να μετατρέψεις σε όγκο και μονάδες",
                icon = Icons.Rounded.InvertColors,
                accent = Color(0xFFFFB36B)
            ) {
                if (reverse) {
                    DecimalField(
                        label = "Μονάδες σύριγγας (U-100)",
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
                        label = "Επιθυμητή ποσότητα (" + targetUnit + ")",
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
                            syringeUnitsPerMl = 100
                        )

                        output = if (!reverse) {
                            val result = PeptideCalculator.dose(
                                vialMg = vialInMg,
                                diluentMl = water,
                                target = targetAmount.toDouble(),
                                targetIsMg = targetUnit == "mg",
                                syringeUnitsPerMl = 100
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
                                syringeUnitsPerMl = 100
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
                    ResultCard(output = output, capacity = syringeCapacity, reverse = reverse)
                }
            }
        }

        item {
            Text(
                "Το εργαλείο υπολογίζει ανασύσταση, συγκέντρωση, όγκο και μονάδες από τιμές που εισάγει ο χρήστης. Δεν προτείνει δοσολογικό πρωτόκολλο ή θεραπεία.",
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
    reverse: Boolean
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
                    label = "Μονάδες σύριγγας (U-100)",
                    value = output[0],
                    emphasize = true
                )
                ResultLine(
                    label = "Όγκος προς άντληση",
                    value = output[1]
                )
                ResultLine(
                    label = "Συγκέντρωση μετά την ανασύσταση",
                    value = output[2]
                )
                ResultLine(
                    label = "Περιεκτικότητα ανά μονάδα U-100",
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
                    label = "Περιεκτικότητα ανά μονάδα U-100",
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
                    "Κλίμακα σύριγγας: " + capacity + " U",
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
