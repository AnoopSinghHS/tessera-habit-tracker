package app.tessera.habit.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import app.tessera.habit.api.HabitApiContract
import app.tessera.habit.ui.components.GithubHeatmapGrid
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.theme.ThemeMode
import app.tessera.habit.ui.viewmodel.TesseraUiState
import app.tessera.habit.widget.HabitCompactWidgetProvider
import app.tessera.habit.widget.HabitHeatmapWidgetProvider
import java.time.LocalDate
import java.util.Locale

@Composable
fun ApiAndSettingsScreen(
    uiState: TesseraUiState,
    onFireLiveBroadcast: (slug: String, mode: String, delta: Int, count: Int?, note: String) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onExportJsonToClipboard: () -> Unit,
    onExportJsonToUri: (Uri) -> Unit,
    onImportJsonFromUri: (Uri) -> Unit,
    onImportJsonFromString: (String) -> Unit,
    onResetDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onPostMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }

    val availableSlugs = remember(uiState.activeHabits) {
        uiState.activeHabits.map { it.habit.slug }
    }
    var selectedSlug by remember(availableSlugs) {
        mutableStateOf(availableSlugs.firstOrNull() ?: "deep-work")
    }
    LaunchedEffect(availableSlugs) {
        if (selectedSlug.isBlank() && availableSlugs.isNotEmpty()) {
            selectedSlug = availableSlugs.first()
        }
    }

    var selectedMode by remember { mutableStateOf(HabitApiContract.MODE_INCREMENT) }
    var noteInput by remember { mutableStateOf("Automated via Tessera API Bridge") }

    var showPasteJsonDialog by remember { mutableStateOf(false) }
    var pasteJsonDraft by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var isDevApiExpanded by remember { mutableStateOf(false) }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            onExportJsonToUri(uri)
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportJsonFromUri(uri)
        }
    }

    if (showPasteJsonDialog) {
        AlertDialog(
            onDismissRequest = { showPasteJsonDialog = false },
            containerColor = colors.surfaceCard,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text("Import JSON Backup Payload", style = TesseraType.SectionTitle) },
            text = {
                OutlinedTextField(
                    value = pasteJsonDraft,
                    onValueChange = { pasteJsonDraft = it },
                    placeholder = {
                        Text(
                            "Paste Tessera JSON export here...",
                            style = TesseraType.CodeSnippet,
                            color = colors.textMuted
                        )
                    },
                    textStyle = TesseraType.CodeSnippet.copy(color = colors.textPrimary),
                    minLines = 6,
                    maxLines = 12,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasteJsonDialog = false
                        if (pasteJsonDraft.isNotBlank()) {
                            onImportJsonFromString(pasteJsonDraft)
                            pasteJsonDraft = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                        contentColor = HabitPalette.EmeraldMatrix.onAccent
                    )
                ) {
                    Text("IMPORT PAYLOAD", style = TesseraType.TelemetryBadge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteJsonDialog = false }) {
                    Text("CANCEL", style = TesseraType.TelemetryBadge, color = colors.textSecondary)
                }
            }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = colors.surfaceCard,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text("Clear Entire Vault?", style = TesseraType.SectionTitle) },
            text = {
                Text(
                    "This deletes all habits and historical check-in squares from the local database.",
                    style = TesseraType.CardSubtitle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitPalette.CrimsonEmber.level4,
                        contentColor = HabitPalette.CrimsonEmber.onAccent
                    )
                ) {
                    Text("CLEAR ALL", style = TesseraType.TelemetryBadge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("CANCEL", style = TesseraType.TelemetryBadge, color = colors.textSecondary)
                }
            }
        )
    }

    val targetHabitWithLogs = remember(uiState.allHabits, selectedSlug) {
        uiState.allHabits.firstOrNull { it.habit.slug.equals(selectedSlug, ignoreCase = true) }
            ?: uiState.activeHabits.firstOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Settings & Atmosphere Header
        item(key = "settings_header") {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "ATMOSPHERE · WIDGETS · LOCAL VAULT",
                    style = TesseraType.TelemetryBadge,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Settings & Preferences",
                    style = TesseraType.DisplayMomentum,
                    color = colors.textPrimary
                )
            }
        }

        // 2. Refined Surface Theme Selector (6 Curated Palettes)
        item(key = "theme_selector") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "ATMOSPHERE & THEME",
                            style = TesseraType.MicroAxis,
                            color = colors.textMuted
                        )
                        Text(
                            text = "Refined architectural palettes tailored for Dynamic AMOLED",
                            style = TesseraType.CardSubtitle,
                            color = colors.textSecondary
                        )
                    }

                    val themePairs = listOf(
                        listOf(ThemeMode.ALABASTER_LIGHT, ThemeMode.OBSIDIAN_DARK),
                        listOf(ThemeMode.ARRAKIS_DUNE, ThemeMode.DEEP_SPACE),
                        listOf(ThemeMode.TATOOINE_DUSK, ThemeMode.SYSTEM)
                    )

                    themePairs.forEach { rowModes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowModes.forEach { mode ->
                                val active = uiState.themeMode == mode
                                val (bgPreview, accentPreview) = when (mode) {
                                    ThemeMode.ALABASTER_LIGHT -> Color(0xFFF5F3EF) to Color(0xFF1C1B19)
                                    ThemeMode.OBSIDIAN_DARK -> Color(0xFF121210) to Color(0xFFF2EFE9)
                                    ThemeMode.ARRAKIS_DUNE -> Color(0xFF13100D) to Color(0xFFD97736)
                                    ThemeMode.DEEP_SPACE -> Color(0xFF090A0D) to Color(0xFF5A82A6)
                                    ThemeMode.TATOOINE_DUSK -> Color(0xFF191414) to Color(0xFFC85A3A)
                                    ThemeMode.SYSTEM -> Color(0xFF202020) to Color(0xFFA0A0A0)
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSetThemeMode(mode) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (active) colors.surfaceElevated else colors.surfaceRecessed,
                                    border = BorderStroke(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = if (active) colors.textPrimary else colors.borderHairline
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(bgPreview)
                                                    .border(1.dp, accentPreview, CircleShape)
                                            )
                                            Text(
                                                text = mode.label,
                                                style = TesseraType.MicroAxis.copy(fontSize = 11.sp),
                                                color = if (active) colors.textPrimary else colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = colors.textPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Samsung Galaxy S26 Ultra Home Screen Widgets Pin & Setup Card
        item(key = "s26_widget_guide") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            tint = HabitPalette.CobaltPulse.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "GALAXY S26 ULTRA HOME SCREEN WIDGETS",
                                style = TesseraType.TelemetryBadge,
                                color = HabitPalette.CobaltPulse.colorForLevel(4, colors.isDark)
                            )
                            Text(
                                text = "High-DPI native Canvas heatmaps with 1-tap check-in",
                                style = TesseraType.CardSubtitle,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "• Wide Matrix (4×2): extended 22-week contribution matrix + streak badge + 1-tap check-in.\n" +
                            "• Compact Tile (2×2): 8-week mini contribution matrix + instant check-in.\n" +
                            "• Long-press your One UI home screen → Widgets → Tessera, or pin directly below:",
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                requestPinWidget(
                                    context = context,
                                    providerClass = HabitHeatmapWidgetProvider::class.java,
                                    onFeedback = onPostMessage
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderSpecular),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PIN 4×2 HEATMAP",
                                style = TesseraType.MicroAxis,
                                color = colors.textPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                requestPinWidget(
                                    context = context,
                                    providerClass = HabitCompactWidgetProvider::class.java,
                                    onFeedback = onPostMessage
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderSpecular),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PIN 2×2 TILE",
                                style = TesseraType.MicroAxis,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }


        // 4. Atomic Habits Methodology Guide (James Clear / Atoms)
        item(key = "atomic_habits_guide") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ATOMIC HABITS METHODOLOGY",
                            style = TesseraType.MicroAxis,
                            color = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                        )
                        Text(
                            text = "JAMES CLEAR",
                            style = TesseraType.MicroAxis,
                            color = colors.textMuted
                        )
                    }

                    Text(
                        text = "Tessera incorporates the 4 Laws of Behavior Change to reduce friction and build lasting identity:",
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AtomicMethodologyRow(
                            title = "Law 1: Make It Obvious (Habit Stacking)",
                            description = "Link habits: 'After [Anchor Habit], I will [New Habit]'."
                        )
                        AtomicMethodologyRow(
                            title = "Law 2: Make It Attractive (Identity Voting)",
                            description = "Every check-in is 1 vote cast for the identity you are forging."
                        )
                        AtomicMethodologyRow(
                            title = "Law 3: Make It Easy (The 2-Minute Rule)",
                            description = "Scale habits down so you can show up effortlessly on low-energy days."
                        )
                        AtomicMethodologyRow(
                            title = "Law 4: Make It Satisfying (Immediate Proof)",
                            description = "Tactile haptic check-ins light up your GitHub contribution squares."
                        )
                        AtomicMethodologyRow(
                            title = "Never Miss Twice (Grace Shield)",
                            description = "Missing once is an accident. Missing twice is the start of a new habit."
                        )
                    }
                }
            }
        }

        // 6. JSON Vault Backup & Portfolio Data Reset
        item(key = "vault_backup_and_reset") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "JSON VAULT BACKUP & DEMO SEED",
                        style = TesseraType.TelemetryBadge,
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                createDocLauncher.launch("tessera-backup-${LocalDate.now()}.json")
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderSpecular),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT FILE", style = TesseraType.MicroAxis, color = colors.textPrimary)
                        }

                        OutlinedButton(
                            onClick = onExportJsonToClipboard,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderSpecular),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPY JSON", style = TesseraType.MicroAxis, color = colors.textPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                openDocLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderSpecular),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("IMPORT FILE", style = TesseraType.MicroAxis, color = colors.textPrimary)
                        }

                        OutlinedButton(
                            onClick = { showPasteJsonDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderSpecular),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Terminal, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PASTE JSON", style = TesseraType.MicroAxis, color = colors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetDemoData,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HabitPalette.EmeraldMatrix.level3),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "RESTORE DEMO DATA",
                                style = TesseraType.MicroAxis,
                                color = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HabitPalette.CrimsonEmber.level3),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteForever,
                                contentDescription = null,
                                tint = HabitPalette.CrimsonEmber.level4,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "CLEAR VAULT",
                                style = TesseraType.MicroAxis,
                                color = HabitPalette.CrimsonEmber.level4
                            )
                        }
                    }
                }
            }
        }
        // 6. Collapsible Developer & Automation API
        item(key = "developer_api_collapsible") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDevApiExpanded = !isDevApiExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    tint = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Developer & Automation API",
                                    style = TesseraType.CardTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (isDevApiExpanded) "Tap to collapse" else "Broadcast intents, deep links & ADB commands",
                                    style = TesseraType.CardSubtitle,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isDevApiExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (isDevApiExpanded) "Collapse" else "Expand",
                            tint = colors.textSecondary
                        )
                    }

                    if (isDevApiExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceCard,
                    border = BorderStroke(1.dp, HabitPalette.EmeraldMatrix.level3.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HabitPalette.EmeraldMatrix.level4)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "HabitApiReceiver (EXPORTED · ACTIVE)",
                                    style = TesseraType.TelemetryBadge,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.lastApiEventSummary,
                                    style = TesseraType.MicroAxis,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                        Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE INTERACTIVE API SANDBOX",
                                style = TesseraType.TelemetryBadge,
                                color = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                            )
                            Text(
                                text = "Dispatch a real Broadcast Intent & watch the matrix light up",
                                style = TesseraType.CardSubtitle,
                                color = colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = HabitPalette.CyberAmber.level4,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Target Habit Slug Selector
                    Text("TARGET RITUAL SLUG", style = TesseraType.MicroAxis, color = colors.textMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSlugs.forEach { slugOption ->
                            val isSelected = slugOption.equals(selectedSlug, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                                        else colors.surfaceElevated
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                                        else colors.borderHairline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedSlug = slugOption }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "#$slugOption",
                                    style = TesseraType.TelemetryBadge,
                                    color = if (isSelected) HabitPalette.EmeraldMatrix.onAccent else colors.textPrimary
                                )
                            }
                        }
                    }

                    // Mode Selector (increment, toggle, set)
                    Text("ACTION MODE", style = TesseraType.MicroAxis, color = colors.textMuted)
                    val modes = listOf(
                        HabitApiContract.MODE_INCREMENT to "INCREMENT (+1)",
                        HabitApiContract.MODE_TOGGLE to "SMART TOGGLE",
                        HabitApiContract.MODE_SET to "COMPLETE TARGET"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modes.forEach { (modeKey, modeLabel) ->
                            val active = selectedMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) colors.textPrimary else colors.surfaceElevated)
                                    .border(1.dp, colors.borderHairline, RoundedCornerShape(8.dp))
                                    .clickable { selectedMode = modeKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeLabel,
                                    style = TesseraType.MicroAxis,
                                    color = if (active) colors.surfaceAbyss else colors.textSecondary
                                )
                            }
                        }
                    }

                    // Optional Payload Note
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Intent Extra 'note'", style = TesseraType.CardSubtitle) },
                        singleLine = true,
                        textStyle = TesseraType.CodeSnippet.copy(color = colors.textPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HabitPalette.EmeraldMatrix.level3,
                            unfocusedBorderColor = colors.borderHairline,
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Live Mini Heatmap Preview of the Target Habit
                    if (targetHabitWithLogs != null) {
                        val targetPalette = HabitPalette.resolve(targetHabitWithLogs.habit.colorKey)
                        val todayCount = targetHabitWithLogs.countForDate(today)
                        val targetPerDay = targetHabitWithLogs.habit.targetPerDay.coerceAtLeast(1)
                        val countsMap = remember(targetHabitWithLogs.logs) {
                            targetHabitWithLogs.logs.associate { it.dateEpochDay to it.count }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.borderHairline, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${targetHabitWithLogs.habit.title.uppercase(Locale.US)} (#${targetHabitWithLogs.habit.slug})",
                                    style = TesseraType.MicroAxis,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "TODAY: $todayCount / $targetPerDay",
                                    style = TesseraType.TelemetryBadge,
                                    color = targetPalette.colorForLevel(4, colors.isDark)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            GithubHeatmapGrid(
                                countsByEpochDay = countsMap,
                                targetPerDay = targetPerDay,
                                palette = targetPalette,
                                weeks = 20,
                                cellSize = 10.dp,
                                cellGap = 2.5.dp,
                                cornerRadius = 2.dp,
                                showWeekdayLabels = false,
                                showMonthLabels = false,
                                scrollable = true,
                                today = today
                            )
                        }
                    }

                    // Fire Broadcast Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val targetCount = if (selectedMode == HabitApiContract.MODE_SET) {
                                targetHabitWithLogs?.habit?.targetPerDay ?: 1
                            } else {
                                null
                            }
                            onFireLiveBroadcast(
                                selectedSlug.ifBlank { "deep-work" },
                                selectedMode,
                                1,
                                targetCount,
                                noteInput
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                            contentColor = HabitPalette.EmeraldMatrix.onAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FIRE BROADCAST INTENT -> #$selectedSlug",
                            style = TesseraType.TelemetryBadge
                        )
                    }


                        Spacer(modifier = Modifier.height(8.dp))

            val adbCmd = remember(selectedSlug, selectedMode, today, noteInput) {
                HabitApiContract.buildAdbBroadcastCommand(
                    slug = selectedSlug,
                    mode = selectedMode,
                    delta = 1,
                    dateIso = today.toString(),
                    note = noteInput.ifBlank { "Automated" }
                )
            }
            val deepLinkUri = remember(selectedSlug, selectedMode, noteInput) {
                HabitApiContract.buildLogDeepLinkUri(
                    slug = selectedSlug,
                    mode = selectedMode,
                    delta = 1,
                    note = noteInput.ifBlank { null }
                ).toString()
            }
            val kotlinSnippet = remember(selectedSlug, selectedMode, noteInput) {
                """
                val intent = Intent("app.tessera.habit.ACTION_LOG_HABIT").apply {
                    setPackage("app.tessera.habit")
                    putExtra("slug", "$selectedSlug")
                    putExtra("mode", "$selectedMode")
                    putExtra("delta", 1)
                    putExtra("note", "${noteInput.replace("\"", "\\\"")}")
                }
                context.sendBroadcast(intent)
                """.trimIndent()
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CodeReferenceCard(
                    title = "ADB / TERMUX / TASKER BROADCAST",
                    code = adbCmd,
                    onCopy = {
                        copyText(context, "ADB Broadcast Command", adbCmd)
                        onPostMessage("Copied ADB broadcast command")
                    }
                )
                CodeReferenceCard(
                    title = "KOTLIN INTER-APP BROADCAST API",
                    code = kotlinSnippet,
                    onCopy = {
                        copyText(context, "Kotlin Intent Snippet", kotlinSnippet)
                        onPostMessage("Copied Kotlin broadcast snippet")
                    }
                )
                CodeReferenceCard(
                    title = "URI DEEP LINK (BIXBY ROUTINES / NFC / SHORTCUTS)",
                    code = deepLinkUri,
                    onCopy = {
                        copyText(context, "Tessera Deep Link", deepLinkUri)
                        onPostMessage("Copied URI deep link")
                    }
                )
            }

                    }
                }
            }
        }

    }
}

@Composable
private fun CodeReferenceCard(
    title: String,
    code: String,
    onCopy: () -> Unit
) {
    val colors = TesseraTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = TesseraType.MicroAxis,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.borderHairline, RoundedCornerShape(6.dp))
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "COPY",
                        style = TesseraType.MicroAxis,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceAbyss)
                    .border(1.dp, colors.borderHairline, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = code,
                    style = TesseraType.CodeSnippet,
                    color = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                )
            }
        }
    }
}

private fun copyText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun requestPinWidget(
    context: Context,
    providerClass: Class<*>,
    onFeedback: (String) -> Unit
) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val provider = ComponentName(context, providerClass)
    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        val requested = appWidgetManager.requestPinAppWidget(provider, null, null)
        if (requested) {
            onFeedback("Confirm widget placement on your One UI home screen")
        } else {
            onFeedback("Long-press home screen -> Widgets -> Tessera to place widget")
        }
    } else {
        onFeedback("Long-press home screen -> Widgets -> Tessera to place widget")
    }
}

@Composable
private fun AtomicMethodologyRow(
    title: String,
    description: String
) {
    val colors = TesseraTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceRecessed)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            style = TesseraType.CardSubtitle,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = TesseraType.MicroAxis,
            color = colors.textSecondary
        )
    }
}
