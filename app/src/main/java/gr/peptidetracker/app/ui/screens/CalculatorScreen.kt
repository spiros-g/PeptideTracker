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
    imageIndex: Map<String, String>,
    onSettings: () -> Unit
) {
    var reverse by remember { mutableStateOf(false) }
    var syringeCapacity by remember { mutableIntStateOf(30) }
    var vialMg by remember { mutableStateOf("5") }
    var diluentMl by remember { mutableStateOf("1") }
    var targetMg by remember { mutableStateOf("0.1") }
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
                title = "Calculator",
                subtitle = "Καθαρή μαθηματική μετατροπή, χωρίς περιττό UI.",
                onSettings = onSettings
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
                            "PEPTIDE CALCULATOR",
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "mg · mL · U-100",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "Δύο κατευθύνσεις μετατροπής με άμεσο αποτέλεσμα.",
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
                    text = "Ποσότητα → U",
                    selected = !reverse,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reverse = false
                        output = emptyList()
                        error = ""
                    }
                )
                ModeChip(
                    text = "U → Ποσότητα",
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
                title = "Σύριγγα",
                subtitle = "Χωρητικότητα κλίμακας U-100",
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
                title = "Φιαλίδιο",
                subtitle = "Συνολική ποσότητα στο φιαλίδιο",
                icon = Icons.Rounded.Science,
                accent = ElectricViolet
            ) {
                ChoiceRow(
                    choices = listOf("5", "10", "20", "30"),
                    selected = vialMg,
                    onSelected = { vialMg = it }
                )
                Spacer(Modifier.height(8.dp))
                DecimalField(
                    label = "Προσαρμοσμένη τιμή (mg)",
                    value = vialMg,
                    onValueChange = { vialMg = it }
                )
            }
        }

        item {
            CalculatorStep(
                number = "03",
                title = "Διαλύτης",
                subtitle = "Συνολικός όγκος σε mL",
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
                    label = "Προσαρμοσμένος όγκος (mL)",
                    value = diluentMl,
                    onValueChange = { diluentMl = it }
                )
            }
        }

        item {
            CalculatorStep(
                number = "04",
                title = if (reverse) "Μονάδες" else "Στόχος",
                subtitle = if (reverse) "Ένδειξη στη σύριγγα U-100" else "Ποσότητα για τη μαθηματική μετατροπή",
                icon = Icons.Rounded.InvertColors,
                accent = Color(0xFFFFB36B)
            ) {
                if (reverse) {
                    DecimalField(
                        label = "Μονάδες U",
                        value = syringeUnits,
                        onValueChange = { syringeUnits = it }
                    )
                } else {
                    ChoiceRow(
                        choices = listOf("0.1", "0.25", "0.5", "1"),
                        selected = targetMg,
                        onSelected = { targetMg = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    DecimalField(
                        label = "Προσαρμοσμένη τιμή (mg)",
                        value = targetMg,
                        onValueChange = { targetMg = it }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    runCatching {
                        val vial = vialMg.toDouble()
                        val water = diluentMl.toDouble()

                        output = if (!reverse) {
                            val result = PeptideCalculator.dose(
                                vialMg = vial,
                                diluentMl = water,
                                target = targetMg.toDouble(),
                                targetIsMg = true,
                                syringeUnitsPerMl = 100
                            )
                            listOf(
                                PeptideCalculator.format(result.syringeUnits, 2) + " U",
                                PeptideCalculator.format(result.volumeMl, 4) + " mL",
                                PeptideCalculator.format(result.concentrationMgPerMl, 3) + " mg/mL"
                            )
                        } else {
                            val result = PeptideCalculator.reverse(
                                vialMg = vial,
                                diluentMl = water,
                                units = syringeUnits.toDouble(),
                                syringeUnitsPerMl = 100
                            )
                            listOf(
                                PeptideCalculator.format(result.amountMg, 4) + " mg",
                                PeptideCalculator.format(result.amountMcg, 2) + " mcg",
                                PeptideCalculator.format(result.volumeMl, 4) + " mL"
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
                "Το εργαλείο εκτελεί μόνο μαθηματικές μετατροπές. Δεν προτείνει ποσότητα, πρωτόκολλο, θεραπεία ή χρήση.",
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
            Text(
                output.first(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            Text(
                output.drop(1).joinToString("  •  "),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
