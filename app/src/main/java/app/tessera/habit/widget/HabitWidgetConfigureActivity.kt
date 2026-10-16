package app.tessera.habit.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tessera.habit.TesseraApp
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

/**
 * Configuration Activity launched when placing or reconfiguring a Tessera Home Screen Widget
 * on the Samsung Galaxy S26 Ultra (One UI 8).
 */
class HabitWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Default to RESULT_CANCELED if user backs out before saving
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        val isCompactWidget = providerInfo?.provider?.className ==
            HabitCompactWidgetProvider::class.java.name

        val dao = TesseraDatabase.getInstance(applicationContext).habitDao()

        setContent {
            TesseraTheme(themeMode = ThemeMode.OBSIDIAN_DARK) {
                val scope = rememberCoroutineScope()
                val habits by dao.observeActiveHabitsWithLogs().collectAsState(initial = emptyList())

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            (application as? TesseraApp)?.repository?.ensureSeeded()
                        }
                    }
                }

                var selectedHabitId by remember {
                    mutableLongStateOf(WidgetPrefs.getHabitId(this, appWidgetId))
                }
                var isDarkWidgetTheme by remember {
                    mutableStateOf(WidgetPrefs.isDarkTheme(this, appWidgetId))
                }
                var selectedTapMode by remember {
                    mutableStateOf(WidgetPrefs.getTapMode(this, appWidgetId))
                }

                LaunchedEffect(habits) {
                    if (habits.isNotEmpty() && habits.none { it.habit.id == selectedHabitId }) {
                        selectedHabitId = habits.first().habit.id
                    }
                }

                val selectedHabit = habits.firstOrNull { it.habit.id == selectedHabitId }
                    ?: habits.firstOrNull()

                WidgetConfigureScreen(
                    habits = habits,
                    selectedHabit = selectedHabit,
                    isCompactWidget = isCompactWidget,
                    isDarkWidgetTheme = isDarkWidgetTheme,
                    selectedTapMode = selectedTapMode,
                    onSelectHabit = { selectedHabitId = it.habit.id },
                    onToggleWidgetTheme = { isDarkWidgetTheme = it },
                    onSelectTapMode = { selectedTapMode = it },
                    onCancel = { finish() },
                    onConfirm = {
                        val chosenHabitId = selectedHabit?.habit?.id ?: -1L
                        scope.launch {
                            WidgetPrefs.saveWidgetConfig(
                                context = applicationContext,
                                appWidgetId = appWidgetId,
                                habitId = chosenHabitId,
                                isDark = isDarkWidgetTheme,
                                tapMode = selectedTapMode
                            )
                            HabitWidgetUpdater.updateSingleWidgetSync(
                                context = applicationContext,
                                appWidgetId = appWidgetId,
                                isCompact = isCompactWidget
                            )
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WidgetConfigureScreen(
    habits: List<HabitWithLogs>,
    selectedHabit: HabitWithLogs?,
    isCompactWidget: Boolean,
    isDarkWidgetTheme: Boolean,
    selectedTapMode: String,
    onSelectHabit: (HabitWithLogs) -> Unit,
    onToggleWidgetTheme: (Boolean) -> Unit,
    onSelectTapMode: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val activePalette = HabitPalette.resolve(selectedHabit?.habit?.colorKey)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.surfaceAbyss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isCompactWidget) {
                            "ONE UI · 2×2 COMPACT WIDGET"
                        } else {
                            "ONE UI · 4×2 MATRIX WIDGET"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = activePalette.level4,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Configure Ritual Widget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cancel",
                        tint = colors.textSecondary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live S26 Ultra Home Screen Preview Card
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "LIVE HOME SCREEN PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textSecondary
                        )

                        val previewBg = if (isDarkWidgetTheme) Color(0xFF0A0C10) else Color(0xFFFFFFFF)
                        val previewBorder = if (isDarkWidgetTheme) Color(0xFF1F242D) else Color(0xFFE2DFD7)
                        val previewTitleColor = if (isDarkWidgetTheme) Color(0xFFF3F4F6) else Color(0xFF111317)
                        val previewSubColor = if (isDarkWidgetTheme) Color(0xFF8A919E) else Color(0xFF686D76)

                        val previewBitmap = remember(selectedHabit, isDarkWidgetTheme, isCompactWidget) {
                            if (isCompactWidget) {
                                WidgetHeatmapBitmapRenderer.renderCompactHeatmap(
                                    context = context,
                                    habitWithLogs = selectedHabit,
                                    isDarkTheme = isDarkWidgetTheme,
                                    weeks = 8
                                ).asImageBitmap()
                            } else {
                                WidgetHeatmapBitmapRenderer.renderWideHeatmap(
                                    context = context,
                                    habitWithLogs = selectedHabit,
                                    isDarkTheme = isDarkWidgetTheme,
                                    weeks = 18
                                ).asImageBitmap()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(previewBg)
                                .border(1.dp, previewBorder, RoundedCornerShape(22.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedHabit?.habit?.title ?: "Select a Ritual",
                                                color = previewTitleColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val streak = selectedHabit?.computeStats()?.currentStreak ?: 0
                                            Text(
                                                text = "${streak}D STREAK",
                                                color = activePalette.level4,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        val todayCount = selectedHabit?.countForDate(LocalDate.now()) ?: 0
                                        val target = selectedHabit?.habit?.targetPerDay ?: 1
                                        val slug = selectedHabit?.habit?.slug ?: "ritual"
                                        Text(
                                            text = "#$slug · $todayCount/$target TODAY",
                                            color = previewSubColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(activePalette.level4),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = activePalette.onAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Widget Heatmap Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isCompactWidget) 76.dp else 88.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                    }
                }

                // Surface Theme Toggle
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "WIDGET SURFACE THEME",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OptionChipCard(
                                title = "Obsidian Dark",
                                subtitle = "#0A0C10 AMOLED",
                                selected = isDarkWidgetTheme,
                                accentColor = activePalette.level4,
                                modifier = Modifier.weight(1f),
                                onClick = { onToggleWidgetTheme(true) }
                            )
                            OptionChipCard(
                                title = "Alabaster Light",
                                subtitle = "#FFFFFF Gallery",
                                selected = !isDarkWidgetTheme,
                                accentColor = activePalette.level4,
                                modifier = Modifier.weight(1f),
                                onClick = { onToggleWidgetTheme(false) }
                            )
                        }
                    }
                }

                // 1-Tap Check-in Mode Toggle
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1-TAP CHECK-IN BEHAVIOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OptionChipCard(
                                title = "Smart Toggle",
                                subtitle = "Cycle 0 → Target → 0",
                                selected = selectedTapMode == WidgetPrefs.TAP_MODE_SMART,
                                accentColor = activePalette.level4,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectTapMode(WidgetPrefs.TAP_MODE_SMART) }
                            )
                            OptionChipCard(
                                title = "Always +1",
                                subtitle = "Unbounded Increment",
                                selected = selectedTapMode == WidgetPrefs.TAP_MODE_INCREMENT,
                                accentColor = activePalette.level4,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectTapMode(WidgetPrefs.TAP_MODE_INCREMENT) }
                            )
                        }
                    }
                }

                // Select Ritual Header
                item {
                    Text(
                        text = "SELECT RITUAL TO BIND (${habits.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textSecondary
                    )
                }

                // Habit List
                items(habits, key = { it.habit.id }) { item ->
                    val palette = HabitPalette.resolve(item.habit.colorKey)
                    val isSelected = item.habit.id == selectedHabit?.habit?.id
                    val stats = remember(item) { item.computeStats() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) colors.surfaceElevated else colors.surfaceCard)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) palette.level4 else colors.borderHairline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectHabit(item) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.level1),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HabitIcons.resolve(item.habit.iconKey),
                                contentDescription = null,
                                tint = palette.level4,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.habit.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "#${item.habit.slug} · ${stats.currentStreak}D STREAK · ${item.habit.targetPerDay}x/day",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.level4),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = palette.onAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Confirm Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceCard)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    enabled = selectedHabit != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activePalette.level4,
                        contentColor = activePalette.onAccent
                    )
                ) {
                    Text(
                        text = "DEPLOY WIDGET TO HOME SCREEN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionChipCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.surfaceElevated else colors.surfaceCard)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accentColor else colors.borderHairline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) colors.textPrimary else colors.textSecondary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = colors.textMuted,
            fontSize = 10.sp
        )
    }
}
