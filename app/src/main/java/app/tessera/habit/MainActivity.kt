package app.tessera.habit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tessera.habit.ui.screens.AnalyticsScreen
import app.tessera.habit.ui.screens.ApiAndSettingsScreen
import app.tessera.habit.ui.screens.DashboardScreen
import app.tessera.habit.ui.screens.HabitDetailSheet
import app.tessera.habit.ui.screens.HabitEditorDialog
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraViewModel

private enum class TesseraTab(
    val label: String,
    val icon: ImageVector
) {
    TODAY("Today", Icons.Rounded.GridView),
    INSIGHTS("Insights", Icons.Rounded.AutoGraph),
    SETTINGS("Settings & API", Icons.Rounded.Terminal)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TesseraViewModel by viewModels {
        TesseraViewModel.factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            viewModel.handleIncomingIntent(intent)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            TesseraTheme(themeMode = uiState.themeMode) {
                TesseraRootApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshApiTelemetrySummary()
    }
}

@Composable
private fun TesseraRootApp(viewModel: TesseraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by rememberSaveable { mutableStateOf(TesseraTab.RITUALS) }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss),
        containerColor = colors.surfaceAbyss,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.textPrimary,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        style = TesseraType.TelemetryBadge,
                        color = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                    )
                }
            }
        },
        bottomBar = {
            TesseraArchitecturalBottomBar(
                currentTab = currentTab,
                onSelectTab = { tab ->
                    currentTab = tab
                    if (tab == TesseraTab.SETTINGS) {
                        viewModel.refreshApiTelemetrySummary()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tabCrossfade") { tab ->
                when (tab) {
                    TesseraTab.TODAY -> {
                        DashboardScreen(
                            uiState = uiState,
                            onCheckInHabit = { habitId -> viewModel.smartCheckIn(habitId) },
                            onOpenHabitDetail = { habitId -> viewModel.openHabitDetail(habitId) },
                            onOpenCreateHabit = { viewModel.openCreateHabitDialog() },
                            onSelectCategory = { cat -> viewModel.selectCategory(cat) },
                            onToggleViewMode = { viewModel.toggleViewMode() },
                            onToggleTheme = { viewModel.cycleThemeMode() },
                            onOpenApiBridge = {
                                viewModel.refreshApiTelemetrySummary()
                                currentTab = TesseraTab.SETTINGS
                            },
                            onResetDemoData = { viewModel.resetDemoData() }
                        )
                    }

                    TesseraTab.INSIGHTS -> {
                        AnalyticsScreen(
                            uiState = uiState,
                            onOpenHabitDetail = { habitId -> viewModel.openHabitDetail(habitId) }
                        )
                    }

                    TesseraTab.SETTINGS -> {
                        ApiAndSettingsScreen(
                            uiState = uiState,
                            onFireLiveBroadcast = { slug, mode, delta, count, note ->
                                viewModel.fireLiveBroadcastTest(
                                    slug = slug,
                                    mode = mode,
                                    delta = delta,
                                    count = count,
                                    note = note
                                )
                            },
                            onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                            onExportJsonToClipboard = {
                                viewModel.exportJsonToString { json ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Tessera Backup JSON", json))
                                }
                            },
                            onExportJsonToUri = { uri -> viewModel.exportJsonToUri(uri) },
                            onImportJsonFromUri = { uri -> viewModel.importJsonFromUri(uri) },
                            onImportJsonFromString = { raw -> viewModel.importJsonFromString(raw) },
                            onResetDemoData = { viewModel.resetDemoData() },
                            onClearAllData = { viewModel.clearAllData() },
                            onPostMessage = { msg -> viewModel.postMessage(msg) }
                        )
                    }
                }
            }
        }
    }

    // Habit Detail & Telemetry Bottom Sheet
    val selectedHabitWithLogs = uiState.selectedHabitWithLogs
    if (selectedHabitWithLogs != null) {
        HabitDetailSheet(
            habitWithLogs = selectedHabitWithLogs,
            onDismiss = { viewModel.closeHabitDetail() },
            onEditHabit = { habit ->
                viewModel.openEditHabitDialog(habit)
            },
            onToggleArchive = { habit ->
                viewModel.toggleArchiveHabit(habit)
            },
            onDeleteHabit = { habitId ->
                viewModel.deleteHabit(habitId)
            },
            onUpdateDateCountAndNote = { habitId, date, count, note ->
                viewModel.setHabitDateCountAndNote(habitId, date, count, note)
            },
            onSmartToggleDate = { habitId, date ->
                viewModel.smartCheckIn(habitId, date)
            },
            onCopiedSlug = { msg ->
                viewModel.postMessage(msg)
            }
        )
    }

    // Create / Edit Ritual Modal Sheet
    if (uiState.isEditorOpen) {
        HabitEditorDialog(
            initialHabit = uiState.editingHabit,
            onDismiss = { viewModel.closeHabitEditor() },
            onSaveHabit = { habit -> viewModel.saveHabit(habit) }
        )
    }
}

@Composable
private fun TesseraArchitecturalBottomBar(
    currentTab: TesseraTab,
    onSelectTab: (TesseraTab) -> Unit
) {
    val colors = TesseraTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceCard)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.borderHairline)
                .padding(top = 1.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TesseraTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                val activeColor = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) colors.surfaceElevated else colors.surfaceCard
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) activeColor.copy(alpha = 0.45f) else colors.surfaceCard,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) activeColor else colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.label,
                            style = TesseraType.MicroAxis,
                            color = if (isSelected) colors.textPrimary else colors.textMuted
                        )
                    }
                }
            }
        }
    }
}
