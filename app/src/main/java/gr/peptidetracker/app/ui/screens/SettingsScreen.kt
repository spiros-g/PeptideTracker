package gr.peptidetracker.app.ui.screens

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
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.BuildConfig
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumFilterChipColors

@Composable
fun SettingsScreen(
    store: LocalStore,
    onBack: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenDataTools: () -> Unit
) {
    var defaultSyringe by remember { mutableIntStateOf(store.defaultSyringeUnitsPerMl()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Ρυθμίσεις",
                subtitle = "Προεπιλογές εφαρμογής και εργαλεία δεδομένων.",
                onBack = onBack
            )
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
                                "Η επιλογή ανοίγει αυτόματα στον υπολογιστή όταν δεν φορτώνεται συγκεκριμένο vial.",
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
                            Icons.Rounded.Backup,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                "Backup & export",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Αποθήκευση backup JSON, επαναφορά και εξαγωγή ιστορικού σε CSV.",
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
                                "Onboarding",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Μπορείς να ξαναδείς τις εισαγωγικές οθόνες χωρίς να διαγραφούν καταγραφές ή απόθεμα.",
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
                        Text("Εμφάνιση onboarding ξανά")
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
                            "Σχετικά",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        "PeptideTrackerGR v" + BuildConfig.VERSION_NAME,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Οι καταγραφές tracker, inventory και progress αποθηκεύονται τοπικά στη συσκευή. Το app δεν επιλέγει ποσότητες ή θεραπευτικές αποφάσεις για τον χρήστη.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
