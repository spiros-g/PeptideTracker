package gr.peptidetracker.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Switch
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
    var darkMode by remember { mutableStateOf(store.darkMode()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPeptide by remember { mutableStateOf<PeptideInfo?>(null) }
    var settingsOpen by remember { mutableStateOf(false) }

    val imageIndex by produceState<Map<String, String>>(initialValue = emptyMap()) {
        value = StoreCatalogClient.loadImageIndex()
    }

    AppTheme(darkMode) {
        PremiumBackground {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (selectedPeptide == null) {
                        PremiumBottomBar(
                            selected = selectedTab,
                            onSelected = { selectedTab = it }
                        )
                    }
                }
            ) { padding ->
                val target = selectedPeptide?.let { "detail:" + it.id } ?: "tab:" + selectedTab

                AnimatedContent(
                    targetState = target,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                    },
                    label = "screenTransition",
                    modifier = Modifier.padding(padding)
                ) {
                    selectedPeptide?.let { peptide ->
                        PeptideDetailScreen(
                            peptide = peptide,
                            store = store,
                            imageIndex = imageIndex,
                            onBack = { selectedPeptide = null }
                        )
                    } ?: when (selectedTab) {
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

            if (settingsOpen) {
                ModalBottomSheet(
                    onDismissRequest = { settingsOpen = false },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    scrimColor = Color.Black.copy(alpha = 0.55f)
                ) {
                    SettingsSheet(
                        darkMode = darkMode,
                        onDarkModeChange = {
                            darkMode = it
                            store.setDarkMode(it)
                        }
                    )
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
        MainDestination("Home", Icons.Rounded.Home),
        MainDestination("Πεπτίδια", Icons.Rounded.Science),
        MainDestination("Calc", Icons.Rounded.Calculate),
        MainDestination("Tracker", Icons.Rounded.QueryStats)
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xEE101722),
                        Color(0xE8141A27),
                        Color(0xEE0D141F)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.11f),
                RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, destination ->
                val active = selected == index
                val tint by animateColorAsState(
                    targetValue = if (active) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(180),
                    label = "navTint"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelected(index) }
                        .background(
                            if (active) ElectricBlue.copy(alpha = 0.12f) else Color.Transparent
                        )
                        .padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        destination.label,
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSheet(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Ρυθμίσεις",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "PeptideTracker GR · v2.0.0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        GlassCard {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dark glass theme", fontWeight = FontWeight.Bold)
                    Text(
                        "Premium dark interface με υψηλή αντίθεση.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange
                )
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
                Text("Local-first δεδομένα", fontWeight = FontWeight.Bold)
                Text(
                    "Οι καταγραφές, το απόθεμα, οι μετρήσεις και τα αγαπημένα αποθηκεύονται στη συσκευή. Η σύνδεση χρησιμοποιείται για τις εικόνες του καταλόγου και για εξωτερικές επιστημονικές πηγές.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(6.dp))
    }
}
