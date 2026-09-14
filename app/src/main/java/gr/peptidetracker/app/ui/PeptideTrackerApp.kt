package gr.peptidetracker.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.PeptideInfo
import gr.peptidetracker.app.data.StoreCatalogClient
import gr.peptidetracker.app.ui.screens.CalculatorScreen
import gr.peptidetracker.app.ui.screens.HomeScreen
import gr.peptidetracker.app.ui.screens.LibraryScreen
import gr.peptidetracker.app.ui.screens.PeptideDetailScreen
import gr.peptidetracker.app.ui.screens.TrackerScreen

private data class MainDestination(
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeptideTrackerApp(store: LocalStore) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPeptide by remember { mutableStateOf<PeptideInfo?>(null) }
    var settingsOpen by remember { mutableStateOf(false) }

    val imageIndex by produceState<Map<String, String>>(initialValue = emptyMap()) {
        value = StoreCatalogClient.loadImageIndex()
    }

    AppTheme {
        PremiumBackground {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                bottomBar = {
                    if (selectedPeptide == null) {
                        PremiumBottomBar(
                            selected = selectedTab,
                            onSelected = { selectedTab = it }
                        )
                    }
                }
            ) { padding ->
                AnimatedContent(
                    targetState = selectedTab to selectedPeptide,
                    transitionSpec = {
                        fadeIn(tween(210)) togetherWith fadeOut(tween(145))
                    },
                    label = "screenTransition",
                    modifier = Modifier.padding(padding)
                ) { (tab, peptide) ->
                    if (peptide != null) {
                        PeptideDetailScreen(
                            peptide = peptide,
                            store = store,
                            imageIndex = imageIndex,
                            onBack = { selectedPeptide = null }
                        )
                    } else {
                        when (tab) {
                            0 -> HomeScreen(
                                store = store,
                                imageIndex = imageIndex,
                                onNavigate = { selectedTab = it },
                                onOpenPeptide = { selectedPeptide = it },
                                onSettings = { settingsOpen = true }
                            )

                            1 -> LibraryScreen(
                                store = store,
                                imageIndex = imageIndex,
                                onOpenPeptide = { selectedPeptide = it },
                                onSettings = { settingsOpen = true }
                            )

                            2 -> CalculatorScreen(
                                imageIndex = imageIndex,
                                onSettings = { settingsOpen = true }
                            )

                            else -> TrackerScreen(
                                store = store,
                                imageIndex = imageIndex,
                                onSettings = { settingsOpen = true }
                            )
                        }
                    }
                }
            }

            if (settingsOpen) {
                ModalBottomSheet(
                    onDismissRequest = { settingsOpen = false },
                    containerColor = GlassSurfaceStrong.copy(alpha = 0.985f),
                    contentColor = TextPrimary,
                    scrimColor = Color.Black.copy(alpha = 0.72f)
                ) {
                    SettingsSheet()
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val destinations = listOf(
        MainDestination("Αρχική", Icons.Rounded.Home),
        MainDestination("Πεπτίδια", Icons.Rounded.Science),
        MainDestination("Υπολογ.", Icons.Rounded.Calculate),
        MainDestination("Ιστορικό", Icons.Rounded.QueryStats)
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xF20C131D),
                        Color(0xF5141D29),
                        Color(0xF20B121B)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.18f),
                        ElectricBlue.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, destination ->
                val active = selected == index
                val tint by animateColorAsState(
                    targetValue = if (active) Color.White else TextMuted,
                    animationSpec = tween(170),
                    label = "navTint"
                )
                val tileColor = if (active) {
                    Brush.linearGradient(
                        listOf(
                            ElectricBlue.copy(alpha = 0.22f),
                            ElectricViolet.copy(alpha = 0.10f)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(19.dp))
                        .clickable { onSelected(index) }
                        .background(tileColor)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (active) ElectricCyan else tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        destination.label,
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSheet() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = ElectricBlue
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Ρυθμίσεις",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    "Ιχνηλάτης Πεπτιδίων · v2.1.0",
                    color = TextSecondary
                )
            }
        }

        GlassCard {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElectricViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.DarkMode,
                        contentDescription = null,
                        tint = ElectricViolet
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        "Μόνιμο σκοτεινό θέμα",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Η εφαρμογή χρησιμοποιεί αποκλειστικά το σκοτεινό glass theme υψηλής αντίθεσης.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Rounded.Shield,
                contentDescription = null,
                tint = ElectricCyan
            )
            Column {
                Text(
                    "Τοπικά δεδομένα",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Οι καταγραφές, το απόθεμα, οι μετρήσεις και τα αγαπημένα αποθηκεύονται στη συσκευή. Η σύνδεση χρησιμοποιείται για stock vial imagery και εξωτερικές επιστημονικές πηγές.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(6.dp))
    }
}
