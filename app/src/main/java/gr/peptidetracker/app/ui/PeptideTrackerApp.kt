package gr.peptidetracker.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import gr.peptidetracker.app.BuildConfig
import gr.peptidetracker.app.data.AppUpdateInfo
import gr.peptidetracker.app.data.AppUpdateInstaller
import gr.peptidetracker.app.data.GitHubUpdateChecker
import gr.peptidetracker.app.data.UpdateDownloadResult
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.peptideCatalog
import gr.peptidetracker.app.ui.screens.CalculatorPreset
import gr.peptidetracker.app.ui.screens.CalculatorScreen
import gr.peptidetracker.app.ui.screens.HomeScreen
import gr.peptidetracker.app.ui.screens.LibraryScreen
import gr.peptidetracker.app.ui.screens.OnboardingScreen
import gr.peptidetracker.app.ui.screens.PeptideDetailScreen
import gr.peptidetracker.app.ui.screens.PrivacyScreen
import gr.peptidetracker.app.ui.screens.RemindersScreen
import gr.peptidetracker.app.ui.screens.SettingsScreen
import gr.peptidetracker.app.ui.screens.TrackerScreen
import kotlinx.coroutines.launch
import java.io.File

private data class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private object Routes {
    const val Home = "home"
    const val Library = "library"
    const val Calculator = "calculator"
    const val Tracker = "tracker"
    const val Reminders = "reminders"
    const val Settings = "settings"
    const val Privacy = "privacy"
    const val Detail = "detail/{peptideId}"

    fun detail(peptideId: String) = "detail/" + Uri.encode(peptideId)
}

@Composable
fun PeptideTrackerApp(
    store: LocalStore,
    updateRequestToken: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showOnboarding by rememberSaveable { mutableStateOf(!store.onboardingComplete()) }
    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var checkingForUpdate by remember { mutableStateOf(false) }
    var installingUpdate by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var pendingUpdateApkPath by remember { mutableStateOf<String?>(null) }
    val githubUpdatesEnabled = remember(context) {
        GitHubUpdateChecker.shouldUseGitHubUpdates(context)
    }
    val uiState: AppUiViewModel = viewModel()
    val navController = rememberNavController()
    val imageIndex = emptyMap<String, String>()

    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val path = pendingUpdateApkPath
        if (!path.isNullOrBlank()) {
            if (AppUpdateInstaller.canInstallPackages(context)) {
                val started = AppUpdateInstaller.launchInstaller(context, File(path))
                updateStatus = if (started) {
                    "Ο εγκαταστάτης Android άνοιξε. Πάτησε Ενημέρωση για ολοκλήρωση."
                } else {
                    "Δεν ήταν δυνατό να ανοίξει ο εγκαταστάτης Android."
                }
            } else {
                updateStatus = "Δεν δόθηκε άδεια εγκατάστασης από αυτή την εφαρμογή."
            }
        }
    }

    fun checkForUpdatesManually() {
        if (!githubUpdatesEnabled || checkingForUpdate || installingUpdate) return
        scope.launch {
            checkingForUpdate = true
            updateStatus = "Έλεγχος για νέα έκδοση…"
            store.markUpdateCheck()
            val update = GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
            availableUpdate = update
            updateStatus = if (update != null) {
                "Η έκδοση v${update.version} είναι διαθέσιμη."
            } else {
                "Έχεις ήδη την τελευταία έκδοση."
            }
            checkingForUpdate = false
        }
    }

    fun installAvailableUpdate() {
        val update = availableUpdate ?: return
        if (installingUpdate || checkingForUpdate) return

        scope.launch {
            installingUpdate = true
            updateStatus = "Λήψη και επαλήθευση του signed APK μέσα από την εφαρμογή…"

            when (val result = AppUpdateInstaller.downloadAndVerify(context, update)) {
                is UpdateDownloadResult.Success -> {
                    pendingUpdateApkPath = result.file.absolutePath
                    installingUpdate = false

                    if (AppUpdateInstaller.canInstallPackages(context)) {
                        val started = AppUpdateInstaller.launchInstaller(context, result.file)
                        updateStatus = if (started) {
                            "Ο εγκαταστάτης Android άνοιξε. Πάτησε Ενημέρωση για ολοκλήρωση."
                        } else {
                            "Δεν ήταν δυνατό να ανοίξει ο εγκαταστάτης Android."
                        }
                    } else {
                        updateStatus =
                            "Επίτρεψε μία φορά εγκατάσταση εφαρμογών από το Peptide Tracker και θα συνεχίσει η ενημέρωση."
                        installPermissionLauncher.launch(
                            AppUpdateInstaller.unknownSourcesSettingsIntent(context)
                        )
                    }
                }

                is UpdateDownloadResult.Error -> {
                    installingUpdate = false
                    updateStatus = result.message
                }
            }
        }
    }

    LaunchedEffect(showOnboarding, updateRequestToken) {
        val forcedByNotification = updateRequestToken > 0
        if (
            !showOnboarding &&
            githubUpdatesEnabled &&
            (forcedByNotification || store.shouldCheckForUpdates())
        ) {
            checkingForUpdate = true
            store.markUpdateCheck()
            val update = GitHubUpdateChecker.check(BuildConfig.VERSION_NAME)
            availableUpdate = update
            if (forcedByNotification) {
                updateStatus = if (update != null) {
                    "Η έκδοση v${update.version} είναι διαθέσιμη."
                } else {
                    "Έχεις ήδη την τελευταία έκδοση."
                }
            }
            checkingForUpdate = false
        }
    }

    val destinations = listOf(
        MainDestination(Routes.Home, "Αρχική", Icons.Rounded.Home),
        MainDestination(Routes.Library, "Πεπτίδια", Icons.Rounded.Science),
        MainDestination(Routes.Calculator, "Υπολογιστής", Icons.Rounded.Calculate),
        MainDestination(Routes.Tracker, "Ημερολόγιο", Icons.Rounded.QueryStats)
    )
    val mainRoutes = destinations.map { it.route }.toSet()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.Home

    fun navigateMain(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.Home) {
                saveState = true
            }
        }
    }

    fun navigateByIndex(index: Int) {
        when (index) {
            0 -> navigateMain(Routes.Home)
            1 -> navigateMain(Routes.Library)
            2 -> navigateMain(Routes.Calculator)
            3 -> navigateMain(Routes.Tracker)
            4 -> navController.navigate(Routes.Reminders) { launchSingleTop = true }
        }
    }

    AppTheme {
        PremiumBackground {
            if (showOnboarding) {
                OnboardingScreen(
                    imageIndex = imageIndex,
                    onFinish = {
                        store.setOnboardingComplete(true)
                        showOnboarding = false
                    }
                )
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = TextPrimary,
                    bottomBar = {
                        if (currentRoute in mainRoutes) {
                            PremiumBottomBar(
                                destinations = destinations,
                                selectedRoute = currentRoute,
                                onSelected = { navigateMain(it) }
                            )
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Home,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Routes.Home) {
                            HomeScreen(
                                store = store,
                                imageIndex = imageIndex,
                                onNavigate = ::navigateByIndex,
                                onOpenPeptide = { peptide ->
                                    navController.navigate(Routes.detail(peptide.id))
                                },
                                onNewLog = {
                                    uiState.requestNewLog()
                                    navigateMain(Routes.Tracker)
                                },
                                onOpenInventory = {
                                    uiState.updateTrackerSection(1)
                                    navigateMain(Routes.Tracker)
                                },
                                onOpenSettings = {
                                    navController.navigate(Routes.Settings) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Routes.Library) {
                            LibraryScreen(
                                store = store,
                                imageIndex = imageIndex,
                                onOpenPeptide = { peptide ->
                                    navController.navigate(Routes.detail(peptide.id))
                                }
                            )
                        }

                        composable(Routes.Calculator) {
                            CalculatorScreen(
                                store = store,
                                imageIndex = imageIndex,
                                preset = uiState.calculatorPreset,
                                defaultSyringeUnitsPerMl = store.defaultSyringeUnitsPerMl(),
                                onPresetConsumed = uiState::consumeCalculatorPreset,
                                onOpenHistory = {
                                    uiState.updateTrackerSection(0)
                                    navigateMain(Routes.Tracker)
                                }
                            )
                        }

                        composable(Routes.Tracker) {
                            TrackerScreen(
                                store = store,
                                imageIndex = imageIndex,
                                initialSection = uiState.trackerSection,
                                newLogRequest = uiState.newLogRequest,
                                inventoryPeptidePreset = uiState.inventoryPreset,
                                dataToolsRequest = uiState.dataToolsRequest,
                                onConsumeNewLogRequest = uiState::consumeNewLogRequest,
                                onConsumeInventoryPreset = uiState::consumeInventoryPreset,
                                onConsumeDataToolsRequest = uiState::consumeDataToolsRequest,
                                onOpenCalculator = { row ->
                                    uiState.updateCalculatorPreset(
                                        CalculatorPreset(
                                            peptideName = row.peptide,
                                            vialMg = row.vialMg,
                                            diluentMl = row.diluentMl,
                                            syringeUnitsPerMl = row.syringeUnitsPerMl
                                        )
                                    )
                                    navigateMain(Routes.Calculator)
                                }
                            )
                        }

                        composable(Routes.Reminders) {
                            RemindersScreen(
                                store = store,
                                imageIndex = imageIndex,
                                onBack = { navController.navigateUp() }
                            )
                        }

                        composable(Routes.Settings) {
                            SettingsScreen(
                                store = store,
                                onBack = { navController.navigateUp() },
                                onReplayOnboarding = {
                                    showOnboarding = true
                                    navController.popBackStack(Routes.Home, inclusive = false)
                                },
                                onOpenDataTools = {
                                    uiState.requestDataTools()
                                    navigateMain(Routes.Tracker)
                                },
                                onOpenPrivacy = {
                                    navController.navigate(Routes.Privacy)
                                },
                                githubUpdatesEnabled = githubUpdatesEnabled,
                                availableUpdateVersion = availableUpdate?.version,
                                updateStatus = updateStatus,
                                updateBusy = checkingForUpdate || installingUpdate,
                                updateInstalling = installingUpdate,
                                onCheckForUpdates = ::checkForUpdatesManually,
                                onInstallUpdate = ::installAvailableUpdate
                            )
                        }

                        composable(Routes.Privacy) {
                            PrivacyScreen(onBack = { navController.navigateUp() })
                        }

                        composable(
                            route = Routes.Detail,
                            arguments = listOf(
                                navArgument("peptideId") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val id = entry.arguments?.getString("peptideId").orEmpty()
                            val peptide = peptideCatalog.firstOrNull { it.id == id }
                            if (peptide != null) {
                                PeptideDetailScreen(
                                    peptide = peptide,
                                    store = store,
                                    imageIndex = imageIndex,
                                    onBack = { navController.navigateUp() },
                                    onOpenCalculator = { item ->
                                        uiState.updateCalculatorPreset(CalculatorPreset(item.name))
                                        navigateMain(Routes.Calculator)
                                    },
                                    onAddInventory = { item ->
                                        uiState.updateTrackerSection(1)
                                        uiState.updateInventoryPreset(item.name)
                                        navigateMain(Routes.Tracker)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            availableUpdate?.let { update ->
                AlertDialog(
                    onDismissRequest = {
                        if (!installingUpdate) availableUpdate = null
                    },
                    title = { Text("Νέα έκδοση ${update.version}") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                buildString {
                                    append("Υπάρχει νεότερη έκδοση του Peptide Tracker.")
                                    if (update.releaseNotes.isNotBlank()) {
                                        append("\n\n")
                                        append(update.releaseNotes.take(700))
                                    }
                                }
                            )
                            if (!updateStatus.isNullOrBlank()) {
                                Text(
                                    updateStatus.orEmpty(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (installingUpdate) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = ::installAvailableUpdate,
                            enabled = !installingUpdate &&
                                !checkingForUpdate &&
                                update.apkUrl != null
                        ) {
                            Text(
                                when {
                                    installingUpdate -> "Προετοιμασία…"
                                    update.apkUrl == null -> "APK μη διαθέσιμο"
                                    else -> "Ενημέρωση τώρα"
                                }
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { availableUpdate = null },
                            enabled = !installingUpdate
                        ) {
                            Text("Αργότερα")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(
    destinations: List<MainDestination>,
    selectedRoute: String,
    onSelected: (String) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
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
            destinations.forEach { destination ->
                val active = selectedRoute == destination.route
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
                        .clickable { onSelected(destination.route) }
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
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
