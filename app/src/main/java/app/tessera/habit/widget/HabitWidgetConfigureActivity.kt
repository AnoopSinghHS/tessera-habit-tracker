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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tessera.habit.R
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.SectionHeading
import app.tessera.habit.ui.components.TesseraCard
import app.tessera.habit.ui.components.TesseraChip
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraDivider
import app.tessera.habit.ui.components.TesseraIconButton
import app.tessera.habit.ui.components.TesseraPrimaryButton
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Shown when a widget is added to the home screen (or reconfigured): pick the habit,
 * the widget's background and what a tap on its check-in button does.
 */
class HabitWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // If the user backs out, the launcher must not add the widget.
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
            TesseraTheme {
                val scope = rememberCoroutineScope()
                // null until the first database read arrives, so the empty state does not flash.
                val loadedHabits: List<HabitWithLogs>? by dao.observeActiveHabitsWithLogs()
                    .collectAsState(initial = null)
                val habits: List<HabitWithLogs> = loadedHabits ?: emptyList()

                var selectedHabitId by remember {
                    mutableLongStateOf(WidgetPrefs.getHabitId(this, appWidgetId))
                }
                var isDarkWidget by remember {
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
                    isLoading = loadedHabits == null,
                    habits = habits,
                    selectedHabit = selectedHabit,
                    isCompactWidget = isCompactWidget,
                    isDarkWidget = isDarkWidget,
                    selectedTapMode = selectedTapMode,
                    onSelectHabit = { selectedHabitId = it.habit.id },
                    onSelectDark = { isDarkWidget = it },
                    onSelectTapMode = { selectedTapMode = it },
                    onCancel = { finish() },
                    onConfirm = {
                        val chosenHabitId = selectedHabit?.habit?.id ?: -1L
                        scope.launch {
                            WidgetPrefs.saveWidgetConfig(
                                context = applicationContext,
                                appWidgetId = appWidgetId,
                                habitId = chosenHabitId,
                                isDark = isDarkWidget,
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
    isLoading: Boolean,
    habits: List<HabitWithLogs>,
    selectedHabit: HabitWithLogs?,
    isCompactWidget: Boolean,
    isDarkWidget: Boolean,
    selectedTapMode: String,
    onSelectHabit: (HabitWithLogs) -> Unit,
    onSelectDark: (Boolean) -> Unit,
    onSelectTapMode: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = TesseraTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TesseraDimens.ScreenPadding,
                    vertical = TesseraDimens.SpaceM
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.widget_configure_title),
                style = TesseraType.Headline,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            TesseraIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "Cancel",
                onClick = onCancel
            )
        }

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(TesseraDimens.SpaceXl),
                contentAlignment = Alignment.Center
            ) {
                if (!isLoading) {
                    Text(
                        text = "Add a habit in Tessera first.",
                        style = TesseraType.Body,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = TesseraDimens.ScreenPadding,
                    vertical = TesseraDimens.SpaceS
                ),
                verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceXl)
            ) {
                item(key = "preview") {
                    Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                        SectionHeading(text = "Preview")
                        WidgetPreview(
                            habit = selectedHabit,
                            isCompactWidget = isCompactWidget,
                            isDarkWidget = isDarkWidget
                        )
                    }
                }

                item(key = "habit") {
                    Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                        SectionHeading(text = "Habit")
                        TesseraCard(contentPadding = 0.dp) {
                            habits.forEachIndexed { index, item ->
                                if (index > 0) {
                                    TesseraDivider(
                                        modifier = Modifier.padding(horizontal = TesseraDimens.SpaceL)
                                    )
                                }
                                RadioRow(
                                    title = item.habit.title,
                                    supporting = null,
                                    selected = item.habit.id == selectedHabit?.habit?.id,
                                    onClick = { onSelectHabit(item) }
                                )
                            }
                        }
                    }
                }

                item(key = "background") {
                    Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                        SectionHeading(text = "Background")
                        Row(horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                            TesseraChip(
                                text = "Light",
                                selected = !isDarkWidget,
                                onClick = { onSelectDark(false) },
                                modifier = Modifier.defaultMinSize(minHeight = TesseraDimens.TouchTarget)
                            )
                            TesseraChip(
                                text = "Dark",
                                selected = isDarkWidget,
                                onClick = { onSelectDark(true) },
                                modifier = Modifier.defaultMinSize(minHeight = TesseraDimens.TouchTarget)
                            )
                        }
                    }
                }

                item(key = "tap") {
                    Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                        SectionHeading(text = "When tapped")
                        TesseraCard(contentPadding = 0.dp) {
                            RadioRow(
                                title = "Mark done / undo",
                                supporting = "Tap to mark today done; tap again to undo. " +
                                    "A habit with a daily target counts up to it first.",
                                selected = selectedTapMode != WidgetPrefs.TAP_MODE_INCREMENT,
                                onClick = { onSelectTapMode(WidgetPrefs.TAP_MODE_SMART) }
                            )
                            TesseraDivider(
                                modifier = Modifier.padding(horizontal = TesseraDimens.SpaceL)
                            )
                            RadioRow(
                                title = "Add one each tap",
                                supporting = "Every tap adds one to today's count and never undoes it.",
                                selected = selectedTapMode == WidgetPrefs.TAP_MODE_INCREMENT,
                                onClick = { onSelectTapMode(WidgetPrefs.TAP_MODE_INCREMENT) }
                            )
                        }
                    }
                }
            }

            TesseraPrimaryButton(
                text = "Add widget",
                onClick = onConfirm,
                enabled = selectedHabit != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TesseraDimens.ScreenPadding,
                        vertical = TesseraDimens.SpaceM
                    )
            )
        }
    }
}

/** A 56dp row with a title, optional explanation and a radio indicator at the end. */
@Composable
private fun RadioRow(
    title: String,
    supporting: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = TesseraDimens.SpaceL, vertical = TesseraDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TesseraType.Body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = TesseraType.Supporting,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.width(TesseraDimens.SpaceM))
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 2.dp,
                    color = if (selected) colors.accent else colors.borderSpecular,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors.accent, CircleShape)
                )
            }
        }
    }
}

/**
 * A stand-in for the widget, drawn with the widget's own colours (not the app theme)
 * and the same heatmap renderer the real widget uses.
 */
@Composable
private fun WidgetPreview(
    habit: HabitWithLogs?,
    isCompactWidget: Boolean,
    isDarkWidget: Boolean
) {
    val context = LocalContext.current
    val widgetColors = WidgetColors.of(isDarkWidget)
    val shape = RoundedCornerShape(TesseraDimens.RadiusCard)

    val heatmap = remember(habit, isDarkWidget, isCompactWidget) {
        if (isCompactWidget) {
            WidgetHeatmapBitmapRenderer.renderCompactHeatmap(
                context = context,
                habitWithLogs = habit,
                isDarkTheme = isDarkWidget
            ).asImageBitmap()
        } else {
            WidgetHeatmapBitmapRenderer.renderWideHeatmap(
                context = context,
                habitWithLogs = habit,
                isDarkTheme = isDarkWidget
            ).asImageBitmap()
        }
    }

    val streak = habit?.computeStats()?.currentStreak ?: 0
    val doneToday = habit?.isCompletedOn(LocalDate.now()) ?: false
    val streakLine = when {
        streak > 0 && isCompactWidget -> if (streak == 1) "1 day" else "$streak days"
        streak > 0 -> "$streak-day streak"
        doneToday -> "Done today"
        else -> "Not done yet"
    }

    Column(
        modifier = Modifier
            .then(if (isCompactWidget) Modifier.width(184.dp) else Modifier.fillMaxWidth())
            .clip(shape)
            .background(Color(widgetColors.surface))
            .border(1.dp, Color(widgetColors.hairline), shape)
            .padding(TesseraDimens.SpaceL),
        verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit?.habit?.title.orEmpty(),
                    style = TesseraType.BodyStrong,
                    color = Color(widgetColors.textPrimary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = streakLine,
                    style = TesseraType.Supporting,
                    color = if (streak > 0) {
                        Color(widgetColors.accent)
                    } else {
                        Color(widgetColors.textSecondary)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceS))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .then(
                        if (doneToday) {
                            Modifier.background(Color(widgetColors.accent), CircleShape)
                        } else {
                            Modifier.border(2.dp, Color(widgetColors.accent), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (doneToday) "✓" else "+",
                    style = TesseraType.Label,
                    color = if (doneToday) {
                        Color(widgetColors.onAccent)
                    } else {
                        Color(widgetColors.accent)
                    }
                )
            }
        }

        Image(
            bitmap = heatmap,
            contentDescription = "Heatmap preview",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(heatmap.width.toFloat() / heatmap.height.toFloat()),
            contentScale = ContentScale.Fit
        )
    }
}
