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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraDivider
import app.tessera.habit.ui.screens.AnalyticsScreen
import app.tessera.habit.ui.screens.ApiAndSettingsScreen
import app.tessera.habit.ui.screens.DashboardScreen
import app.tessera.habit.ui.screens.HabitDetailSheet
import app.tessera.habit.ui.screens.HabitEditorDialog
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraViewModel

private enum class TesseraTab(
    val label: String,
    val icon: ImageVector
) {
    TODAY("Today", Icons.Rounded.GridView),
    PROGRESS("Progress", Icons.Rounded.BarChart),
    SETTINGS("Settings", Icons.Rounded.Tune)
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
            TesseraTheme {
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
        viewModel.refreshApiSummary()
    }
}

@Composable
private fun TesseraRootApp(viewModel: TesseraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by rememberSaveable { mutableStateOf(TesseraTab.TODAY) }

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
                    modifier = Modifier.padding(
                        horizontal = TesseraDimens.SpaceL,
                        vertical = TesseraDimens.SpaceS
                    ),
                    containerColor = colors.textPrimary,
                    contentColor = colors.surfaceCard,
                    shape = RoundedCornerShape(TesseraDimens.RadiusControl)
                ) {
                    Text(
                        text = data.visuals.message,
                        style = TesseraType.Supporting,
                        color = colors.surfaceCard
                    )
                }
            }
        },
        bottomBar = {
            TesseraBottomBar(
                currentTab = currentTab,
                onSelectTab = { tab ->
                    currentTab = tab
                    if (tab == TesseraTab.SETTINGS) {
                        viewModel.refreshApiSummary()
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
                            onToggleViewMode = { viewModel.toggleViewMode() }
                        )
                    }

                    TesseraTab.PROGRESS -> {
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
                            onExportJsonToClipboard = {
                                viewModel.exportJsonToString { json ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Tessera backup", json))
                                }
                            },
                            onExportJsonToUri = { uri -> viewModel.exportJsonToUri(uri) },
                            onImportJsonFromUri = { uri -> viewModel.importJsonFromUri(uri) },
                            onImportJsonFromString = { raw -> viewModel.importJsonFromString(raw) },
                            onClearAllData = { viewModel.clearAllData() },
                            onPostMessage = { msg -> viewModel.postMessage(msg) }
                        )
                    }
                }
            }
        }
    }

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

    if (uiState.isEditorOpen) {
        HabitEditorDialog(
            draft = uiState.editorDraft,
            isEditing = uiState.editingHabit != null && (uiState.editingHabit?.id ?: 0L) > 0L,
            onUpdateDraft = { draft -> viewModel.updateDraft(draft) },
            onDiscardDraft = { viewModel.discardDraft() },
            onDismiss = { viewModel.closeHabitEditor() },
            onSaveHabit = { habit -> viewModel.saveHabit(habit) }
        )
    }
}

@Composable
private fun TesseraBottomBar(
    currentTab: TesseraTab,
    onSelectTab: (TesseraTab) -> Unit
) {
    val colors = TesseraTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceCard)
    ) {
        TesseraDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = TesseraDimens.SpaceS, vertical = TesseraDimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TesseraTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = TesseraDimens.TouchTarget)
                        .clip(RoundedCornerShape(TesseraDimens.RadiusControl))
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelectTab(tab) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) colors.surfaceElevated else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.textPrimary else colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
                    Text(
                        text = tab.label,
                        style = TesseraType.Caption,
                        color = if (isSelected) colors.textPrimary else colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
