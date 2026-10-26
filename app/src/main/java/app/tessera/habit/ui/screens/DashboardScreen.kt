package app.tessera.habit.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitCard
import app.tessera.habit.ui.components.HabitCompactBentoCard
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import java.time.LocalTime

@Composable
fun DashboardScreen(
    uiState: TesseraUiState,
    onCheckInHabit: (Long) -> Unit,
    onOpenHabitDetail: (Long) -> Unit,
    onOpenCreateHabit: () -> Unit,
    onOpenRitualChamber: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenApiBridge: () -> Unit,
    onResetDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val today = remember { LocalDate.now() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Centered Editorial Date Header ("Friday, Oct 16" + "3 of 4 habits completed")
        item(key = "editorial_header") {
            EditorialDashboardHeader(
                activeHabits = uiState.activeHabits,
                isCompactBentoView = uiState.isCompactBentoView,
                isDarkTheme = colors.isDark,
                today = today,
                onToggleViewMode = onToggleViewMode,
                onToggleTheme = onToggleTheme,
                onOpenCreateHabit = onOpenCreateHabit
            )
        }

        // 2. Fabulous Guided Ritual Flow Banner
        if (uiState.activeHabits.isNotEmpty()) {
            item(key = "guided_ritual_card") {
                val currentHour = remember { LocalTime.now().hour }
                val recommendedTrack = when {
                    currentHour < 12 -> "DAWN"
                    currentHour < 18 -> "FOCUS"
                    else -> "TWILIGHT"
                }
                val trackTitle = when (recommendedTrack) {
                    "DAWN" -> "Dawn Ritual"
                    "FOCUS" -> "Focus Ritual"
                    else -> "Twilight Ritual"
                }
                val trackHabits = uiState.activeHabits.filter {
                    it.habit.ritualTime.equals(recommendedTrack, ignoreCase = true)
                }
                val pendingInTrack = trackHabits.filter { it.countForDate(today) < it.habit.targetPerDay }
                val isTrackFinished = trackHabits.isNotEmpty() && pendingInTrack.isEmpty()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenRitualChamber(recommendedTrack) },
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceCard,
                    border = BorderStroke(1.dp, HabitPalette.AmberWarm.level3.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(HabitPalette.AmberWarm.colorForLevel(1, colors.isDark)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (recommendedTrack) {
                                        "DAWN" -> Icons.Rounded.WbSunny
                                        "FOCUS" -> Icons.Rounded.Bolt
                                        else -> Icons.Rounded.NightsStay
                                    },
                                    contentDescription = null,
                                    tint = HabitPalette.AmberWarm.colorForLevel(4, colors.isDark),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "FABULOUS GUIDED RITUAL",
                                    style = TesseraType.MicroAxis,
                                    color = HabitPalette.AmberWarm.colorForLevel(4, colors.isDark)
                                )
                                Text(
                                    text = trackTitle,
                                    style = TesseraType.CardTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = when {
                                        trackHabits.isEmpty() -> "Start step-by-step guided focus chamber"
                                        isTrackFinished -> "All ${trackHabits.size} rituals completed today · Tap to review"
                                        else -> "${pendingInTrack.size} of ${trackHabits.size} rituals remaining · 2m timer"
                                    },
                                    style = TesseraType.CardSubtitle,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HabitPalette.AmberWarm.colorForLevel(4, colors.isDark))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isTrackFinished) "REVIEW" else "BEGIN →",
                                style = TesseraType.TelemetryBadge,
                                color = HabitPalette.AmberWarm.onAccentFor(colors.isDark)
                            )
                        }
                    }
                }
            }
        }

        // 2. Subtle Category Filter Strip
        if (uiState.categories.size > 1) {
            item(key = "category_strip") {
                CategoryFilterStrip(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    activeCount = uiState.activeHabits.size,
                    archivedCount = uiState.archivedHabits.size,
                    onSelectCategory = onSelectCategory
                )
            }
        }

        // 3. Habit Cards (Asymmetric Split Cards or 2-Column Compact Bento Grid)
        if (uiState.filteredHabits.isEmpty()) {
            item(key = "empty_state") {
                EmptyHabitsEditorialCard(
                    isArchivedFilter = uiState.selectedCategory.equals("ARCHIVED", ignoreCase = true),
                    onOpenCreateHabit = onOpenCreateHabit,
                    onResetDemoData = onResetDemoData
                )
            }
        } else if (uiState.isCompactBentoView) {
            val pairedRows = uiState.filteredHabits.chunked(2)
            items(
                items = pairedRows,
                key = { pair -> "bento_row_${pair.first().habit.id}" }
            ) { rowHabits ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val first = rowHabits[0]
                    HabitCompactBentoCard(
                        habitWithLogs = first,
                        onCheckInClick = { onCheckInHabit(first.habit.id) },
                        onCardClick = { onOpenHabitDetail(first.habit.id) },
                        today = today,
                        modifier = Modifier.weight(1f)
                    )
                    if (rowHabits.size > 1) {
                        val second = rowHabits[1]
                        HabitCompactBentoCard(
                            habitWithLogs = second,
                            onCheckInClick = { onCheckInHabit(second.habit.id) },
                            onCardClick = { onOpenHabitDetail(second.habit.id) },
                            today = today,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            items(
                items = uiState.filteredHabits,
                key = { it.habit.id }
            ) { habitWithLogs ->
                HabitCard(
                    habitWithLogs = habitWithLogs,
                    onCheckInClick = { onCheckInHabit(habitWithLogs.habit.id) },
                    onCardClick = { onOpenHabitDetail(habitWithLogs.habit.id) },
                    today = today
                )
            }
        }
    }
}

@Composable
private fun EditorialDashboardHeader(
    activeHabits: List<HabitWithLogs>,
    isCompactBentoView: Boolean,
    isDarkTheme: Boolean,
    today: LocalDate,
    onToggleViewMode: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenCreateHabit: () -> Unit
) {
    val colors = TesseraTheme.colors
    val formattedDate = remember(today) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)
        today.format(formatter)
    }

    val totalActive = activeHabits.size
    val completedTodayCount = remember(activeHabits, today) {
        activeHabits.count { it.isCompletedOn(today) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimalist utility bar above or aligned with editorial header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIconButton(
                icon = if (isDarkTheme) HabitIcons.MorningSun else HabitIcons.EveningMoon,
                contentDescription = "Toggle theme",
                onClick = onToggleTheme
            )
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButton(
                icon = if (isCompactBentoView) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                contentDescription = if (isCompactBentoView) "Full card view" else "Compact grid view",
                onClick = onToggleViewMode
            )
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "New habit",
                onClick = onOpenCreateHabit
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = formattedDate,
            style = TesseraType.EditorialDateHeader,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$completedTodayCount of $totalActive habits completed",
            style = TesseraType.CardSubtitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surfaceCard)
            .border(1.dp, colors.borderHairline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun CategoryFilterStrip(
    categories: List<String>,
    selectedCategory: String,
    activeCount: Int,
    archivedCount: Int,
    onSelectCategory: (String) -> Unit
) {
    val colors = TesseraTheme.colors
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = category.equals(selectedCategory, ignoreCase = true)
            val label = when {
                category.equals("ALL", ignoreCase = true) -> "All ($activeCount)"
                category.equals("ARCHIVED", ignoreCase = true) -> "Archived ($archivedCount)"
                else -> category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
                }
            }

            AnimatedContent(targetState = isSelected, label = "categoryPill") { active ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (active) colors.textPrimary else colors.surfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (active) colors.textPrimary else colors.borderHairline,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = TesseraType.TelemetryBadge,
                        color = if (active) colors.surfaceCard else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHabitsEditorialCard(
    isArchivedFilter: Boolean,
    onOpenCreateHabit: () -> Unit,
    onResetDemoData: () -> Unit
) {
    val colors = TesseraTheme.colors
    val accent = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = HabitIcons.OctahedronPrism,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isArchivedFilter) "No Archived Habits" else "Begin Your Daily Practice",
                style = TesseraType.SectionTitle,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArchivedFilter) {
                    "Archived habits rest quietly in your local archive without appearing on your daily canvas."
                } else {
                    "Create a daily habit to begin recording your contribution heatmap, or restore the sample habits."
                },
                style = TesseraType.CardSubtitle,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            if (!isArchivedFilter) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenCreateHabit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = HabitPalette.EmeraldMatrix.onAccentFor(colors.isDark)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Habit", style = TesseraType.TelemetryBadge)
                    }

                    OutlinedButton(
                        onClick = onResetDemoData,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderSpecular)
                    ) {
                        Text(
                            text = "Restore Samples",
                            style = TesseraType.TelemetryBadge,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
