package app.tessera.habit.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.model.HabitStats
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.GithubHeatmapGrid
import app.tessera.habit.ui.components.HeatmapIntensityLegend
import app.tessera.habit.ui.components.StatRow
import app.tessera.habit.ui.components.TesseraCard
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraDivider
import app.tessera.habit.ui.components.TesseraIconButton
import app.tessera.habit.ui.components.TesseraSecondaryButton
import app.tessera.habit.ui.components.tesseraTextFieldColors
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

/** Weeks shown in the history heatmap (it scrolls sideways). */
private const val HISTORY_WEEKS = 52

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailSheet(
    habitWithLogs: HabitWithLogs,
    onDismiss: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onToggleArchive: (HabitEntity) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onUpdateDateCountAndNote: (habitId: Long, date: LocalDate, count: Int, note: String) -> Unit,
    // Not called here: the toggle it drives clears a day that is already done, and in this
    // sheet only the minus button may lower a count. "Mark done" sets the count instead.
    onSmartToggleDate: (habitId: Long, date: LocalDate) -> Unit,
    onCopiedSlug: (String) -> Unit
) {
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val habit = habitWithLogs.habit
    val target = habit.targetPerDay.coerceAtLeast(1)
    val today = remember { LocalDate.now() }
    val stats = remember(habitWithLogs, today) { habitWithLogs.computeStats(today) }
    val countsByEpochDay = remember(habitWithLogs.logs) {
        habitWithLogs.logs.associate { it.dateEpochDay to it.count }
    }

    var selectedDate by remember { mutableStateOf(today) }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val savedCount = habitWithLogs.countForDate(selectedDate)
    val savedNote = habitWithLogs.noteForDate(selectedDate)
    // Local copies so quick taps act on the latest value, not the last one read from storage.
    var countDraft by remember(selectedDate, savedCount) { mutableStateOf(savedCount) }
    var noteDraft by remember(selectedDate, savedNote) { mutableStateOf(savedNote) }

    // The heatmap's first column starts on the Monday (HISTORY_WEEKS - 1) weeks back.
    val historyStart = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .minusWeeks((HISTORY_WEEKS - 1).toLong())
    }
    val historyDaysShown = (today.toEpochDay() - historyStart.toEpochDay() + 1L).toInt()
    val historyDaysDone = remember(countsByEpochDay, historyStart, today, target) {
        val from = historyStart.toEpochDay()
        val to = today.toEpochDay()
        countsByEpochDay.count { (epochDay, count) -> epochDay in from..to && count >= target }
    }

    LaunchedEffect(selectedDate) {
        displayedMonth = YearMonth.from(selectedDate)
    }

    val selectDate: (LocalDate) -> Unit = { date ->
        if (!date.isAfter(today)) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedDate = date
        }
    }
    val writeCount: (Int) -> Unit = { newCount ->
        val safeCount = newCount.coerceAtLeast(0)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        countDraft = safeCount
        // The note goes along with every count change so it is never dropped.
        onUpdateDateCountAndNote(habit.id, selectedDate, safeCount, noteDraft.trim())
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.surfaceCard,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text("Delete ${habit.title}?", style = TesseraType.Title) },
            text = {
                Text(
                    text = "Its whole history will be removed. This can't be undone.",
                    style = TesseraType.Body
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteHabit(habit.id)
                    },
                    modifier = Modifier.heightIn(min = TesseraDimens.TouchTarget)
                ) {
                    Text("Delete", style = TesseraType.Label, color = colors.statusDanger)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.heightIn(min = TesseraDimens.TouchTarget)
                ) {
                    Text("Cancel", style = TesseraType.Label, color = colors.textPrimary)
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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = TesseraDimens.ScreenPadding,
                    vertical = TesseraDimens.SpaceS
                )
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceM)
        ) {
            DetailTopBar(
                title = habit.title,
                onClose = onDismiss,
                onEdit = { onEditHabit(habit) }
            )

            StreakHero(currentStreak = stats.currentStreak)

            TesseraCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last $HISTORY_WEEKS weeks",
                        style = TesseraType.Label,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$historyDaysDone of $historyDaysShown days",
                        style = TesseraType.Supporting,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))
                GithubHeatmapGrid(
                    countsByEpochDay = countsByEpochDay,
                    targetPerDay = habit.targetPerDay,
                    modifier = Modifier.fillMaxWidth(),
                    weeks = HISTORY_WEEKS,
                    showWeekdayLabels = true,
                    showMonthLabels = true,
                    scrollable = true,
                    today = today,
                    selectedDate = selectedDate,
                    onDateClick = selectDate
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))
                HeatmapIntensityLegend(modifier = Modifier.align(Alignment.End))
            }

            SelectedDayCard(
                date = selectedDate,
                today = today,
                count = countDraft,
                target = target,
                unitLabel = habit.unitLabel,
                noteDraft = noteDraft,
                noteChanged = noteDraft.trim() != savedNote.trim(),
                onNoteChange = { noteDraft = it },
                onMinus = { if (countDraft > 0) writeCount(countDraft - 1) },
                onPlus = { writeCount(countDraft + 1) },
                onMarkDone = { if (countDraft < target) writeCount(target) },
                onSaveNote = {
                    onUpdateDateCountAndNote(habit.id, selectedDate, countDraft, noteDraft.trim())
                    onCopiedSlug("Note saved")
                }
            )

            MonthCalendarCard(
                displayedMonth = displayedMonth,
                today = today,
                selectedDate = selectedDate,
                habitWithLogs = habitWithLogs,
                onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                onNextMonth = {
                    val next = displayedMonth.plusMonths(1)
                    if (!next.isAfter(YearMonth.from(today))) {
                        displayedMonth = next
                    }
                },
                // Tapping a day only selects it; changes are made in the day card above.
                onDayClick = selectDate
            )

            StatsCard(stats = stats, target = target, unitLabel = habit.unitLabel)

            val details = listOf(
                "Who this makes me" to habit.identityStatement,
                "When" to habit.cueAnchor,
                "Two-minute version" to habit.microStarter,
                "Reward" to habit.temptationReward
            ).filter { it.second.isNotBlank() }
            if (details.isNotEmpty() || habit.description.isNotBlank()) {
                DetailsCard(
                    showHeading = details.isNotEmpty(),
                    rows = if (habit.description.isNotBlank()) {
                        details + ("Notes" to habit.description)
                    } else {
                        details
                    }
                )
            }

            TesseraCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automation ID",
                            style = TesseraType.Body,
                            color = colors.textSecondary
                        )
                        Text(
                            text = habit.slug,
                            style = TesseraType.Code,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(TesseraDimens.SpaceM))
                    QuietIconButton(
                        icon = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy automation ID",
                        onClick = {
                            copyToClipboard(context, "Tessera automation ID", habit.slug)
                            onCopiedSlug("Copied ${habit.slug}")
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TesseraDimens.SpaceS, bottom = TesseraDimens.SpaceL),
                verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)
            ) {
                TesseraSecondaryButton(
                    text = if (habit.isArchived) "Restore" else "Archive",
                    onClick = { onToggleArchive(habit) },
                    modifier = Modifier.fillMaxWidth(),
                    icon = if (habit.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive
                )
                TesseraSecondaryButton(
                    text = "Delete habit",
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    danger = true,
                    icon = Icons.Rounded.DeleteOutline
                )
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onClose: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TesseraIconButton(
            icon = Icons.Rounded.ChevronLeft,
            contentDescription = "Close",
            onClick = onClose,
            filled = false
        )
        Spacer(modifier = Modifier.width(TesseraDimens.SpaceXs))
        Text(
            text = title,
            style = TesseraType.Title,
            color = TesseraTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(TesseraDimens.SpaceXs))
        TesseraIconButton(
            icon = Icons.Rounded.Edit,
            contentDescription = "Edit habit",
            onClick = onEdit,
            filled = false
        )
    }
}

@Composable
private fun StreakHero(currentStreak: Int) {
    val colors = TesseraTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TesseraDimens.SpaceXs)
    ) {
        Row {
            Text(
                text = currentStreak.toString(),
                style = TesseraType.Display,
                color = colors.accent,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceM))
            Text(
                text = "day streak",
                style = TesseraType.Body,
                color = colors.textSecondary,
                modifier = Modifier.alignByBaseline()
            )
        }
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
        if (currentStreak <= 0) {
            Text(
                text = "Start a streak today",
                style = TesseraType.Body,
                color = colors.textSecondary
            )
        } else {
            val previous = previousMilestone(currentStreak)
            val next = nextMilestone(currentStreak)
            val remaining = next - currentStreak
            MilestoneTrack(streak = currentStreak, previous = previous, next = next)
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = previous.toString(),
                    style = TesseraType.Caption,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = "${daysText(remaining)} to $next",
                    style = TesseraType.Label,
                    color = colors.textPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = next.toString(),
                    style = TesseraType.Caption,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

/** A line from the previous milestone to the next, filled up to the current streak. */
@Composable
private fun MilestoneTrack(streak: Int, previous: Int, next: Int) {
    val colors = TesseraTheme.colors
    val trackColor = colors.borderHairline
    val accent = colors.accent
    val span = (next - previous).coerceAtLeast(1)
    val fraction = ((streak - previous).toFloat() / span.toFloat()).coerceIn(0f, 1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(TesseraDimens.SpaceXl)
    ) {
        val haloRadius = 10.dp.toPx()
        val dotRadius = 6.dp.toPx()
        val strokeWidth = 2.dp.toPx()
        val y = size.height / 2f
        val minX = haloRadius
        val maxX = size.width - haloRadius
        val x = if (maxX > minX) (size.width * fraction).coerceIn(minX, maxX) else size.width / 2f

        drawLine(
            color = trackColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent,
            start = Offset(0f, y),
            end = Offset(x, y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawCircle(color = accent.copy(alpha = 0.2f), radius = haloRadius, center = Offset(x, y))
        drawCircle(color = accent, radius = dotRadius, center = Offset(x, y))
    }
}

@Composable
private fun SelectedDayCard(
    date: LocalDate,
    today: LocalDate,
    count: Int,
    target: Int,
    unitLabel: String,
    noteDraft: String,
    noteChanged: Boolean,
    onNoteChange: (String) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onMarkDone: () -> Unit,
    onSaveNote: () -> Unit
) {
    val colors = TesseraTheme.colors
    val dateText = remember(date, today) {
        if (date == today) {
            "Today, " + date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.US))
        } else {
            date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US))
        }
    }
    val isDone = count >= target

    TesseraCard {
        Text(
            text = dateText,
            style = TesseraType.Label,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuietIconButton(
                icon = Icons.Rounded.Remove,
                contentDescription = "One less",
                onClick = onMinus,
                enabled = count > 0
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$count of $target",
                    style = TesseraType.Title,
                    color = if (isDone) colors.accent else colors.textPrimary
                )
                val unitText = if (target > 1) unitLabel.trim() else ""
                if (unitText.isNotEmpty()) {
                    Text(
                        text = unitText,
                        style = TesseraType.Supporting,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            QuietIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "One more",
                onClick = onPlus
            )
        }
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))
        TesseraSecondaryButton(
            text = if (isDone) "Done" else "Mark done",
            onClick = onMarkDone,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDone,
            icon = Icons.Rounded.Check
        )
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
        OutlinedTextField(
            value = noteDraft,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note", style = TesseraType.Supporting) },
            textStyle = TesseraType.Body,
            maxLines = 4,
            colors = tesseraTextFieldColors(),
            shape = RoundedCornerShape(TesseraDimens.RadiusControl)
        )
        if (noteChanged) {
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
            TesseraSecondaryButton(
                text = "Save note",
                onClick = onSaveNote,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun MonthCalendarCard(
    displayedMonth: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    habitWithLogs: HabitWithLogs,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val colors = TesseraTheme.colors
    val monthTitle = remember(displayedMonth) {
        displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
    }
    // Monday-first grid.
    val leadingEmptyCells = displayedMonth.atDay(1).dayOfWeek.value - 1
    val daysInMonth = displayedMonth.lengthOfMonth()
    val rowCount = (leadingEmptyCells + daysInMonth + 6) / 7
    val canGoNext = displayedMonth.isBefore(YearMonth.from(today))
    val cellShape = RoundedCornerShape(TesseraDimens.RadiusControl)
    val cellInset = TesseraDimens.SpaceXs / 2

    TesseraCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                style = TesseraType.Title,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            QuietIconButton(
                icon = Icons.Rounded.ChevronLeft,
                contentDescription = "Previous month",
                onClick = onPreviousMonth
            )
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceS))
            QuietIconButton(
                icon = Icons.Rounded.ChevronRight,
                contentDescription = "Next month",
                onClick = onNextMonth,
                enabled = canGoNext
            )
        }

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { weekday ->
                Text(
                    text = weekday,
                    style = TesseraType.Caption,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))

        for (rowIndex in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (columnIndex in 0..6) {
                    val dayNumber = rowIndex * 7 + columnIndex - leadingEmptyCells + 1
                    if (dayNumber !in 1..daysInMonth) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(CalendarCellHeight)
                        )
                    } else {
                        val cellDate = displayedMonth.atDay(dayNumber)
                        val isFuture = cellDate.isAfter(today)
                        val isSelected = cellDate == selectedDate
                        val isToday = cellDate == today
                        val level = if (isFuture) 0 else habitWithLogs.intensityLevelForDate(cellDate)
                        val background = if (isFuture) Color.Transparent else colors.heatLevel(level)
                        val textColor = when {
                            isFuture -> colors.textSecondary.copy(alpha = 0.6f)
                            level >= 3 -> colors.onAccent
                            else -> colors.textPrimary
                        }
                        val ringColor = when {
                            isSelected -> colors.textPrimary
                            isToday -> colors.accent
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(CalendarCellHeight)
                                .clip(cellShape)
                                .clickable(enabled = !isFuture) { onDayClick(cellDate) }
                                .padding(cellInset),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(cellShape)
                                    .background(background)
                                    .border(2.dp, ringColor, cellShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = TesseraType.Caption,
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

private val CalendarCellHeight = 44.dp

@Composable
private fun StatsCard(
    stats: HabitStats,
    target: Int,
    unitLabel: String
) {
    val last30Done = (stats.completionRate30Days * 30f).roundToInt().coerceIn(0, 30)
    val rows = buildList {
        add("Best streak" to daysText(stats.bestStreak))
        add("Last 30 days" to "$last30Done of 30")
        add("This week" to "${stats.weeklyCompletedDays} of 7")
        add(
            "All time" to if (stats.totalCompletions == 1) {
                "1 check-in"
            } else {
                "${stats.totalCompletions} check-ins"
            }
        )
        if (target > 1) {
            add("Daily target" to "$target ${unitLabel.trim()}".trim())
        }
    }

    TesseraCard(contentPadding = TesseraDimens.SpaceL) {
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                TesseraDivider()
            }
            StatRow(label = row.first, value = row.second)
        }
    }
}

/** The optional "why and how" answers and the habit's notes, label above value so long text wraps. */
@Composable
private fun DetailsCard(
    showHeading: Boolean,
    rows: List<Pair<String, String>>
) {
    val colors = TesseraTheme.colors
    TesseraCard {
        if (showHeading) {
            Text(
                text = "Why and how",
                style = TesseraType.Label,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
        }
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                TesseraDivider()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TesseraDimens.SpaceM)
            ) {
                Text(
                    text = row.first,
                    style = TesseraType.Supporting,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
                Text(
                    text = row.second.trim(),
                    style = TesseraType.Body,
                    color = colors.textPrimary
                )
            }
        }
    }
}

/** A round 48dp icon button that stays visible on a card. */
@Composable
private fun QuietIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = Modifier
            .size(TesseraDimens.TouchTarget)
            .clip(CircleShape)
            .background(colors.surfaceElevated)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        )
    }
}

private val StreakMilestones = listOf(7, 14, 30, 60, 100, 200, 365)

/** The milestone the streak is heading for: the ladder, then every further 365 days. */
private fun nextMilestone(streak: Int): Int {
    return StreakMilestones.firstOrNull { it > streak } ?: ((streak / 365) + 1) * 365
}

/** The last milestone reached, or 0 before the first. */
private fun previousMilestone(streak: Int): Int {
    return if (streak >= 365) {
        (streak / 365) * 365
    } else {
        StreakMilestones.lastOrNull { it <= streak } ?: 0
    }
}

private fun daysText(days: Int): String = if (days == 1) "1 day" else "$days days"

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}
