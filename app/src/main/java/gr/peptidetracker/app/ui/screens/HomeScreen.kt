package gr.peptidetracker.app.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.PeptideInfo
import gr.peptidetracker.app.data.peptideCatalog
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.FloatingVial
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage

@Composable
fun HomeScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    onNavigate: (Int) -> Unit,
    onOpenPeptide: (PeptideInfo) -> Unit
) {
    val entries = store.entries()
    val inventory = store.inventory()
    val favoriteIds = store.favorites()
    val favorites = peptideCatalog.filter { it.id in favoriteIds }
    val last = entries.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Ιχνηλάτης Πεπτιδίων",
                subtitle = "Το κέντρο ελέγχου των πεπτιδίων σου."
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(274.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = { onNavigate(2) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    ElectricBlue.copy(alpha = 0.20f),
                                    ElectricViolet.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            "Υπολογιστής Ανασύστασης & Δοσολογίας",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            "Συγκέντρωση, όγκος και μονάδες U-100 από τις τιμές που εισάγεις.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Άνοιγμα υπολογιστή",
                                color = ElectricBlue,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(Modifier.width(5.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    StoreVialImage(
                        productKey = "retatrutide",
                        imageIndex = imageIndex,
                        modifier = Modifier.size(width = 104.dp, height = 190.dp),
                        contentDescription = "Φιαλίδιο πεπτιδίου"
                    )
                }
            }
        }

        item {
            Text(
                "Γρήγορη πρόσβαση",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Πεπτίδια",
                    subtitle = peptideCatalog.size.toString() + " προφίλ",
                    icon = Icons.Rounded.Science,
                    accent = ElectricViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(1) }
                )
                QuickActionCard(
                    title = "Καταγραφές",
                    subtitle = entries.size.toString() + " καταγραφές",
                    icon = Icons.AutoMirrored.Rounded.EventNote,
                    accent = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(3) }
                )
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    value = entries.size.toString(),
                    label = "Καταγρ.",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = inventory.sumOf { it.quantity }.toString(),
                    label = "Φιαλίδια",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = favorites.size.toString(),
                    label = "Αγαπημ.",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Τελευταία δραστηριότητα",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(3) }
            ) {
                if (last == null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.EventNote,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Δεν υπάρχουν καταγραφές", fontWeight = FontWeight.Bold)
                            Text(
                                "Οι καταγραφές είναι έτοιμες για την πρώτη εγγραφή.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StoreVialImage(
                            productKey = last.peptide,
                            imageIndex = imageIndex,
                            modifier = Modifier.size(width = 52.dp, height = 72.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(last.peptide, fontWeight = FontWeight.ExtraBold)
                            Text(
                                last.amount,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                            if (last.note.isNotBlank()) {
                                Text(
                                    last.note,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (favorites.isNotEmpty()) {
            item {
                Text(
                    "Αγαπημένα",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    items(favorites, key = { it.id }) { peptide ->
                        FavoritePeptideCard(
                            peptide = peptide,
                            imageIndex = imageIndex,
                            onClick = { onOpenPeptide(peptide) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.height(134.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(23.dp)
                )
            }
            Column {
                Text(title, fontWeight = FontWeight.ExtraBold)
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(92.dp),
        contentPadding = PaddingValues(13.dp)
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
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FavoritePeptideCard(
    peptide: PeptideInfo,
    imageIndex: Map<String, String>,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(148.dp)
            .height(174.dp),
        contentPadding = PaddingValues(12.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StoreVialImage(
                productKey = peptide.id,
                imageIndex = imageIndex,
                modifier = Modifier
                    .height(104.dp)
                    .fillMaxWidth()
            )
            Text(
                peptide.name,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                peptide.category,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
