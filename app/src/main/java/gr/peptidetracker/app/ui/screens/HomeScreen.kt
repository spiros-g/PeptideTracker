package gr.peptidetracker.app.ui.screens

import gr.peptidetracker.app.i18n.t

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WarningAmber
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
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    onNavigate: (Int) -> Unit,
    onOpenPeptide: (PeptideInfo) -> Unit,
    onNewLog: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val entries = store.entries()
    val inventory = store.inventory()
    val favoriteIds = store.favorites()
    val favorites = peptideCatalog.filter { it.id in favoriteIds }
    val last = entries.firstOrNull()
    val activeVial = inventory.firstOrNull { it.active && it.quantity > 0 }
    val now = System.currentTimeMillis()
    val expiryWarningWindow = now + 30L * 24L * 60L * 60L * 1000L
    val expiredCount = inventory.count {
        it.quantity > 0 && it.expiryDate != null && it.expiryDate < now
    }
    val expiringSoonCount = inventory.count {
        it.quantity > 0 &&
            it.expiryDate != null &&
            it.expiryDate in now..expiryWarningWindow
    }
    val nextReminder = store.reminders()
        .filter { it.enabled && it.scheduledAt >= now }
        .minByOrNull { it.scheduledAt }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            PremiumTopBar(
                title = "Peptide Tracker",
                subtitle = t("Το κέντρο ελέγχου για ημερολόγιο, απόθεμα και υπολογισμούς."),
                actionIcon = Icons.Rounded.Settings,
                actionDescription = t("Ρυθμίσεις"),
                onAction = onOpenSettings
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 198.dp),
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
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            t("Υπολογιστής ανασύστασης"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            t("Βάλε ποσότητα φιαλιδίου και διαλύτη για άμεση μαθηματική μετατροπή."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                t("Άνοιγμα υπολογιστή"),
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
                        modifier = Modifier.size(width = 82.dp, height = 148.dp),
                        contentDescription = t("Φιαλίδιο πεπτιδίου")
                    )
                }
            }
        }

        item {
            Text(
                t("Γρήγορη πρόσβαση"),
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = t("Πεπτίδια"),
                    subtitle = peptideCatalog.size.toString() + t(" προφίλ"),
                    icon = Icons.Rounded.Science,
                    accent = ElectricViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(1) }
                )
                QuickActionCard(
                    title = t("Ημερολόγιο"),
                    subtitle = entries.size.toString() + t(" χρήσεις πεπτιδίων"),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = t("Νέα καταγραφή"),
                    subtitle = t("Πρόσθεσε χρήση"),
                    icon = Icons.Rounded.AddCircle,
                    accent = ElectricBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onNewLog
                )
                QuickActionCard(
                    title = t("Απόθεμα"),
                    subtitle = inventory.sumOf { it.quantity }.toString() + t(" φιαλίδια"),
                    icon = Icons.Rounded.Inventory2,
                    accent = ElectricViolet,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenInventory
                )
            }
        }

        item {
            QuickActionCard(
                title = t("Πλάνο & υπενθυμίσεις"),
                subtitle = if (nextReminder == null) t("Οργάνωσε τις δικές σου υπενθυμίσεις") else t("Δες ή άλλαξε την επόμενη υπενθύμιση"),
                icon = Icons.Rounded.NotificationsActive,
                accent = ElectricCyan,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(4) }
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    value = entries.size.toString(),
                    label = t("Χρήσεις"),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = inventory.sumOf { it.quantity }.toString(),
                    label = t("Φιαλίδια"),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = favorites.size.toString(),
                    label = t("Αγαπημ."),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (nextReminder != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigate(4) }
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
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
                                t("Επόμενη υπενθύμιση"),
                                color = ElectricCyan,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                nextReminder.peptide,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                DateFormat.getDateTimeInstance(
                                    DateFormat.MEDIUM,
                                    DateFormat.SHORT
                                ).format(Date(nextReminder.scheduledAt)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
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

        if (activeVial != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenInventory
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElectricCyan.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Ενεργό φιαλίδιο"),
                                color = ElectricCyan,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(activeVial.peptide, fontWeight = FontWeight.ExtraBold)
                            Text(
                                activeVial.effectiveRemainingMg.toString() + t(" mg υπόλοιπο · ") +
                                    activeVial.quantity + t(" φιαλίδια συνολικά"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (activeVial.isReconstituted) {
                                Text(
                                    (activeVial.diluentMl?.toString() ?: "") + " mL · U-" +
                                        activeVial.syringeUnitsPerMl +
                                        (activeVial.mcgPerSyringeUnit?.let { " · " + it.toString() + " mcg/U" } ?: ""),
                                    color = ElectricViolet,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
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

        if (expiredCount > 0 || expiringSoonCount > 0) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenInventory
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Έλεγχος αποθέματος"),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                buildString {
                                    if (expiredCount > 0) {
                                        append(expiredCount)
                                        append(if (expiredCount == 1) t(" καταχώρηση έχει λήξει") else t(" καταχωρήσεις έχουν λήξει"))
                                    }
                                    if (expiredCount > 0 && expiringSoonCount > 0) append(" · ")
                                    if (expiringSoonCount > 0) {
                                        append(expiringSoonCount)
                                        append(if (expiringSoonCount == 1) t(" λήγει εντός 30 ημερών") else t(" λήγουν εντός 30 ημερών"))
                                    }
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
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

        item {
            Text(
                t("Τελευταία δραστηριότητα"),
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
                            Text(t("Δεν έχεις καταγράψει χρήση"), fontWeight = FontWeight.Bold)
                            Text(
                                t("Όταν καταγράψεις ότι χρησιμοποίησες ένα πεπτίδιο, θα εμφανιστεί εδώ η τελευταία σου εγγραφή."),
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
                    t("Αγαπημένα"),
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
        modifier = modifier.heightIn(min = 134.dp),
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
        modifier = modifier.heightIn(min = 92.dp),
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
            .heightIn(min = 174.dp),
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
