package app.tessera.habit.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tessera.habit.api.HabitApiContract
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.GithubHeatmapGrid
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.components.HeatmapIntensityLegend
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.HabitPaletteSpec
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailSheet(
    habitWithLogs: HabitWithLogs,
    onDismiss: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onToggleArchive: (HabitEntity) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onUpdateDateCountAndNote: (habitId: Long, date: LocalDate, count: Int, note: String) -> Unit,
    onSmartToggleDate: (habitId: Long, date: LocalDate) -> Unit,
    onCopiedSlug: (String) -> Unit
) {
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val habit = habitWithLogs.habit
    val palette = remember(habit.colorKey) { HabitPalette.resolve(habit.colorKey) }
    val today = remember { LocalDate.now() }
    val stats = remember(habitWithLogs, today) { habitWithLogs.computeStats(today) }
    val countsByEpochDay = remember(habitWithLogs.logs) {
        habitWithLogs.logs.associate { it.dateEpochDay to it.count }
    }

    var selectedDate by remember { mutableStateOf(today) }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val selectedCount = habitWithLogs.countForDate(selectedDate)
    val selectedSavedNote = habitWithLogs.noteForDate(selectedDate)
    var noteDraft by remember(selectedDate, selectedSavedNote) {
        mutableStateOf(selectedSavedNote)
    }

    LaunchedEffect(selectedDate) {
        displayedMonth = YearMonth.from(selectedDate)
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.surfaceCard,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text("Delete \"${habit.title}\"?", style = TesseraType.SectionTitle) },
            text = {
                Text(
                    "This permanently removes the ritual and all ${stats.totalCompletions} historical contribution logs.",
                    style = TesseraType.CardSubtitle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteHabit(habit.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitPalette.CrimsonEmber.level4,
                        contentColor = HabitPalette.CrimsonEmber.onAccent
                    )
                ) {
                    Text("DELETE", style = TesseraType.TelemetryBadge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", style = TesseraType.TelemetryBadge, color = colors.textSecondary)
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceAbyss,
        contentColor = colors.textPrimary,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Header with Icon, Title, Slug Chip, and Edit/Archive/Delete Actions
            DetailHeaderBar(
                habit = habit,
                palette = palette,
                onCopySlug = {
                    val deepLink = HabitApiContract.buildLogDeepLinkUri(slug = habit.slug).toString()
                    copyToClipboard(context, "Tessera Deep Link", deepLink)
                    onCopiedSlug("Copied $deepLink")
                },
                onEdit = { onEditHabit(habit) },
                onArchive = { onToggleArchive(habit) },
                onDelete = { showDeleteConfirm = true },
                onClose = onDismiss
            )

            // 2. 2x2 Monospace Telemetry Bento Grid
            TelemetryBentoGrid(
                currentStreak = stats.currentStreak,
                bestStreak = stats.bestStreak,
                consistency30d = (stats.completionRate30Days * 100f).roundToInt(),
                totalCompletions = stats.totalCompletions,
                palette = palette
            )

            // 3. Full-Year 52-Week Scrollable GitHub Heatmap Explorer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                            text = "52-WEEK CONTRIBUTION MATRIX",
                            style = TesseraType.TelemetryBadge,
                            color = palette.colorForLevel(4, colors.isDark)
                        )
                        HeatmapIntensityLegend(palette = palette)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GithubHeatmapGrid(
                        countsByEpochDay = countsByEpochDay,
                        targetPerDay = habit.targetPerDay,
                        palette = palette,
                        weeks = 52,
                        cellSize = 11.dp,
                        cellGap = 3.dp,
                        cornerRadius = 2.5.dp,
                        showWeekdayLabels = true,
                        showMonthLabels = true,
                        scrollable = true,
                        today = today,
                        selectedDate = selectedDate,
                        onDateClick = { clickedDate ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedDate = clickedDate
                        }
                    )
                }
            }

            // 4. Selected Date Inspector & Daily Field Note Editor
            SelectedDayInspectorCard(
                selectedDate = selectedDate,
                count = selectedCount,
                targetPerDay = habit.targetPerDay,
                unitLabel = habit.unitLabel,
                noteDraft = noteDraft,
                savedNote = selectedSavedNote,
                palette = palette,
                onNoteChange = { noteDraft = it },
                onAdjustCount = { newCount ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdateDateCountAndNote(habit.id, selectedDate, newCount.coerceAtLeast(0), noteDraft)
                },
                onSaveNote = {
                    onUpdateDateCountAndNote(habit.id, selectedDate, selectedCount, noteDraft.trim())
                    onCopiedSlug("Saved note for $selectedDate")
                }
            )

            // 5. Interactive Monthly Calendar Back-Fill View
            MonthlyBackfillCalendarCard(
                displayedMonth = displayedMonth,
                today = today,
                selectedDate = selectedDate,
                habitWithLogs = habitWithLogs,
                palette = palette,
                onPrevMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                onNextMonth = {
                    val next = displayedMonth.plusMonths(1)
                    if (!next.isAfter(YearMonth.from(today))) {
                        displayedMonth = next
                    }
                },
                onDayClick = { clickedDate ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedDate = clickedDate
                    onSmartToggleDate(habit.id, clickedDate)
                }
            )

            // 6. Weekday Rhythm Distribution Telemetry Card
            WeekdayDistributionCard(
                weekdayDistribution = stats.weekdayDistribution,
                palette = palette
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailHeaderBar(
    habit: HabitEntity,
    palette: HabitPaletteSpec,
    onCopySlug: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val colors = TesseraTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.colorForLevel(1, colors.isDark))
                    .border(1.dp, palette.level3.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HabitIcons.resolve(habit.iconKey),
                    contentDescription = habit.title,
                    tint = palette.colorForLevel(4, colors.isDark),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = TesseraType.SectionTitle,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons Cluster
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniHeaderActionBtn(Icons.Rounded.Edit, "Edit", onEdit)
                MiniHeaderActionBtn(
                    if (habit.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                    "Archive",
                    onArchive
                )
                MiniHeaderActionBtn(Icons.Rounded.DeleteOutline, "Delete", onDelete)
                MiniHeaderActionBtn(Icons.Rounded.Close, "Close", onClose)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Programmatic Slug & Category Metadata Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceCard)
                    .border(1.dp, colors.borderHairline, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = habit.category.uppercase(Locale.US),
                    style = TesseraType.MicroAxis,
                    color = colors.textSecondary
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceCard)
                    .border(1.dp, colors.borderHairline, RoundedCornerShape(6.dp))
                    .clickable(onClick = onCopySlug)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "tessera://log?slug=${habit.slug}",
                    style = TesseraType.MicroAxis,
                    color = palette.colorForLevel(4, colors.isDark)
                )
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy deep link",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniHeaderActionBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun TelemetryBentoGrid(
    currentStreak: Int,
    bestStreak: Int,
    consistency30d: Int,
    totalCompletions: Int,
    palette: HabitPaletteSpec
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryMetricTile(
                label = "CURRENT STREAK",
                value = "$currentStreak DAYS",
                subtitle = "Active sequence",
                accentColor = palette.colorForLevel(4, TesseraTheme.colors.isDark),
                modifier = Modifier.weight(1f)
            )
            TelemetryMetricTile(
                label = "BEST STREAK",
                value = "$bestStreak DAYS",
                subtitle = "All-time record",
                accentColor = TesseraTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryMetricTile(
                label = "30D CONSISTENCY",
                value = "$consistency30d%",
                subtitle = "Rolling 30-day rate",
                accentColor = palette.colorForLevel(4, TesseraTheme.colors.isDark),
                modifier = Modifier.weight(1f)
            )
            TelemetryMetricTile(
                label = "TOTAL VOLUME",
                value = "$totalCompletions",
                subtitle = "Lifetime check-ins",
                accentColor = TesseraTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryMetricTile(
    label: String,
    value: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = TesseraType.MicroAxis,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = TesseraType.TelemetryHero,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = TesseraType.CardSubtitle,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun SelectedDayInspectorCard(
    selectedDate: LocalDate,
    count: Int,
    targetPerDay: Int,
    unitLabel: String,
    noteDraft: String,
    savedNote: String,
    palette: HabitPaletteSpec,
    onNoteChange: (String) -> Unit,
    onAdjustCount: (Int) -> Unit,
    onSaveNote: () -> Unit
) {
    val colors = TesseraTheme.colors
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US) }
    val safeTarget = targetPerDay.coerceAtLeast(1)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderSpecular)
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
                Column {
                    Text(
                        text = "SELECTED DAY INSPECTOR",
                        style = TesseraType.MicroAxis,
                        color = palette.colorForLevel(4, colors.isDark)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedDate.format(dateFormatter).uppercase(Locale.US),
                        style = TesseraType.TelemetryMedium,
                        color = colors.textPrimary
                    )
                }

                // Stepper Controls (-, count/target, +, Complete)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniHeaderActionBtn(
                        icon = Icons.Rounded.Remove,
                        contentDescription = "Decrease count",
                        onClick = { onAdjustCount((count - 1).coerceAtLeast(0)) }
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.borderHairline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count / $safeTarget ${unitLabel.uppercase(Locale.US)}",
                            style = TesseraType.TelemetryBadge,
                            color = if (count >= safeTarget) palette.colorForLevel(4, colors.isDark) else colors.textPrimary
                        )
                    }

                    MiniHeaderActionBtn(
                        icon = Icons.Rounded.Add,
                        contentDescription = "Increase count",
                        onClick = { onAdjustCount(count + 1) }
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (count >= safeTarget) palette.colorForLevel(4, colors.isDark)
                                else colors.surfaceElevated
                            )
                            .border(1.dp, palette.level3, RoundedCornerShape(9.dp))
                            .clickable {
                                val next = if (count >= safeTarget) 0 else safeTarget
                                onAdjustCount(next)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Complete target",
                            tint = if (count >= safeTarget) palette.onAccent else palette.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Daily Field Note Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = onNoteChange,
                    placeholder = {
                        Text(
                            text = "Add field note for this date (e.g., weight, pages, session reflection)...",
                            style = TesseraType.CardSubtitle,
                            color = colors.textMuted
                        )
                    },
                    textStyle = TesseraType.CardSubtitle.copy(color = colors.textPrimary),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.level3,
                        unfocusedBorderColor = colors.borderHairline,
                        focusedContainerColor = colors.surfaceElevated,
                        unfocusedContainerColor = colors.surfaceElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                if (noteDraft.trim() != savedNote.trim()) {
                    Button(
                        onClick = onSaveNote,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.colorForLevel(4, colors.isDark),
                            contentColor = palette.onAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SAVE", style = TesseraType.TelemetryBadge)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyBackfillCalendarCard(
    displayedMonth: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    habitWithLogs: HabitWithLogs,
    palette: HabitPaletteSpec,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val colors = TesseraTheme.colors
    val monthTitle = remember(displayedMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
        displayedMonth.format(formatter).uppercase(Locale.US)
    }

    val firstOfMonth = remember(displayedMonth) { displayedMonth.atDay(1) }
    val leadingEmptyCells = remember(firstOfMonth) { firstOfMonth.dayOfWeek.value - 1 } // Monday = 0
    val daysInMonth = remember(displayedMonth) { displayedMonth.lengthOfMonth() }
    val totalCells = remember(leadingEmptyCells, daysInMonth) {
        ((leadingEmptyCells + daysInMonth + 6) / 7) * 7
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                Column {
                    Text(
                        text = "MONTHLY BACK-FILL CALENDAR",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                    Text(
                        text = monthTitle,
                        style = TesseraType.TelemetryMedium,
                        color = colors.textPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniHeaderActionBtn(Icons.Rounded.ChevronLeft, "Previous Month", onPrevMonth)
                    MiniHeaderActionBtn(Icons.Rounded.ChevronRight, "Next Month", onNextMonth)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Header Row
            val dowHeaders = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            Row(modifier = Modifier.fillMaxWidth()) {
                dowHeaders.forEach { dow ->
                    Text(
                        text = dow,
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 7-Column Calendar Rows
            val rows = totalCells / 7
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                for (rowIdx in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        for (colIdx in 0..6) {
                            val cellIndex = rowIdx * 7 + colIdx
                            val dayNumber = cellIndex - leadingEmptyCells + 1
                            if (dayNumber !in 1..daysInMonth) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1.25f))
                            } else {
                                val cellDate = displayedMonth.atDay(dayNumber)
                                val isFuture = cellDate.isAfter(today)
                                val isSelected = cellDate == selectedDate
                                val level = habitWithLogs.intensityLevelForDate(cellDate)

                                val bg = when {
                                    isFuture -> colors.surfaceRecessed.copy(alpha = 0.25f)
                                    level > 0 -> palette.colorForLevel(level, colors.isDark)
                                    else -> colors.surfaceRecessed
                                }
                                val textColor = when {
                                    isFuture -> colors.textMuted.copy(alpha = 0.4f)
                                    level >= 3 -> palette.onAccent
                                    else -> colors.textPrimary
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.25f)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(bg)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) colors.textPrimary else colors.borderHairline,
                                            shape = RoundedCornerShape(7.dp)
                                        )
                                        .clickable(enabled = !isFuture) { onDayClick(cellDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = TesseraType.TelemetryBadge,
                                        color = textColor
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

@Composable
private fun WeekdayDistributionCard(
    weekdayDistribution: List<Int>,
    palette: HabitPaletteSpec
) {
    val colors = TesseraTheme.colors
    val maxCount = (weekdayDistribution.maxOrNull() ?: 1).coerceAtLeast(1)
    val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "WEEKDAY RHYTHM DISTRIBUTION",
                style = TesseraType.MicroAxis,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 0..6) {
                    val count = weekdayDistribution.getOrElse(i) { 0 }
                    val fraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                    val level = when {
                        count == 0 -> 0
                        fraction >= 0.8f -> 4
                        fraction >= 0.5f -> 3
                        else -> 2
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = count.toString(),
                            style = TesseraType.MicroAxis,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.68f)
                                .height(54.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(colors.surfaceRecessed),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(palette.colorForLevel(level, colors.isDark))
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = dayNames[i],
                            style = TesseraType.MicroAxis,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}
