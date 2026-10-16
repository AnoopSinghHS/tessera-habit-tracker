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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitCard
import app.tessera.habit.ui.components.HabitCompactBentoCard
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    uiState: TesseraUiState,
    onCheckInHabit: (Long) -> Unit,
    onOpenHabitDetail: (Long) -> Unit,
    onOpenCreateHabit: () -> Unit,
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Editorial Bar & Today's Rituals Momentum HUD
        item(key = "momentum_header") {
            MomentumEditorialHeader(
                activeHabits = uiState.activeHabits,
                isCompactBentoView = uiState.isCompactBentoView,
                isDarkTheme = colors.isDark,
                today = today,
                onToggleViewMode = onToggleViewMode,
                onToggleTheme = onToggleTheme,
                onOpenApiBridge = onOpenApiBridge,
                onOpenCreateHabit = onOpenCreateHabit
            )
        }

        // 2. Category Filter Pill Strip
        item(key = "category_strip") {
            CategoryFilterStrip(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                activeCount = uiState.activeHabits.size,
                archivedCount = uiState.archivedHabits.size,
                onSelectCategory = onSelectCategory
            )
        }

        // 3. Habit Cards (Architectural Full Cards OR 2-Column Bento Compact Grid)
        if (uiState.filteredHabits.isEmpty()) {
            item(key = "empty_state") {
                EmptyRitualsArchitecturalCard(
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
private fun MomentumEditorialHeader(
    activeHabits: List<HabitWithLogs>,
    isCompactBentoView: Boolean,
    isDarkTheme: Boolean,
    today: LocalDate,
    onToggleViewMode: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenApiBridge: () -> Unit,
    onOpenCreateHabit: () -> Unit
) {
    val colors = TesseraTheme.colors
    val dateOverline = remember(today) {
        val formatter = DateTimeFormatter.ofPattern("EEE · MMM d", Locale.US)
        val weekOfYear = today.get(WeekFields.ISO.weekOfWeekBasedYear())
        "${today.format(formatter).uppercase(Locale.US)} · WEEK $weekOfYear"
    }

    val totalActive = activeHabits.size
    val completedTodayCount = remember(activeHabits, today) {
        activeHabits.count { it.isCompletedOn(today) }
    }
    val momentumPct = if (totalActive > 0) {
        ((completedTodayCount.toFloat() / totalActive.toFloat()) * 100f).roundToInt()
    } else {
        0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Editorial Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateOverline,
                    style = TesseraType.TelemetryBadge,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tessera",
                    style = TesseraType.DisplayMomentum,
                    color = colors.textPrimary
                )
            }

            // Right Action Cluster
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    icon = if (isCompactBentoView) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                    contentDescription = if (isCompactBentoView) "Architectural Cards" else "2-Column Bento Grid",
                    onClick = onToggleViewMode
                )

                HeaderIconButton(
                    icon = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Toggle Theme",
                    onClick = onToggleTheme
                )

                HeaderIconButton(
                    icon = Icons.Rounded.Terminal,
                    contentDescription = "API Bridge",
                    onClick = onOpenApiBridge
                )

                // Primary "+ Ritual" Trigger
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark))
                        .clickable(onClick = onOpenCreateHabit)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "New Ritual",
                            tint = HabitPalette.EmeraldMatrix.onAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NEW",
                            style = TesseraType.TelemetryBadge,
                            color = HabitPalette.EmeraldMatrix.onAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Today's Rituals Momentum HUD Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.surfaceCard,
            border = BorderStroke(1.dp, colors.borderHairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S MOMENTUM",
                        style = TesseraType.TelemetryBadge,
                        color = colors.textSecondary
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "%02d / %02d RITUALS · %d%%",
                            completedTodayCount,
                            totalActive,
                            momentumPct
                        ),
                        style = TesseraType.TelemetryBadge,
                        color = if (completedTodayCount == totalActive && totalActive > 0) {
                            HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                        } else {
                            colors.textPrimary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Segmented Per-Habit Chromatic Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (activeHabits.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.surfaceRecessed)
                        )
                    } else {
                        activeHabits.forEach { hwl ->
                            val palette = HabitPalette.resolve(hwl.habit.colorKey)
                            val level = hwl.intensityLevelForDate(today)
                            val segColor = if (level > 0) {
                                palette.colorForLevel(level, colors.isDark)
                            } else {
                                colors.surfaceRecessed
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(segColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(18.dp)
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
                category.equals("ALL", ignoreCase = true) -> "ALL ($activeCount)"
                category.equals("ARCHIVED", ignoreCase = true) -> "ARCHIVED ($archivedCount)"
                else -> category.uppercase(Locale.US)
            }

            AnimatedContent(targetState = isSelected, label = "categoryPill") { active ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) colors.textPrimary else colors.surfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (active) colors.textPrimary else colors.borderHairline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = TesseraType.TelemetryBadge,
                        color = if (active) colors.surfaceAbyss else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRitualsArchitecturalCard(
    isArchivedFilter: Boolean,
    onOpenCreateHabit: () -> Unit,
    onResetDemoData: () -> Unit
) {
    val colors = TesseraTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isArchivedFilter) "No Archived Rituals" else "Zero Active Rituals",
                style = TesseraType.SectionTitle,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArchivedFilter) {
                    "Archived habits remain stored in your local vault without cluttering your daily momentum header."
                } else {
                    "Start building your contribution matrix by defining a daily ritual or restoring the 16-week showcase portfolio."
                },
                style = TesseraType.CardSubtitle,
                color = colors.textSecondary
            )
            if (!isArchivedFilter) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenCreateHabit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                            contentColor = HabitPalette.EmeraldMatrix.onAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CREATE RITUAL", style = TesseraType.TelemetryBadge)
                    }

                    OutlinedButton(
                        onClick = onResetDemoData,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.borderSpecular)
                    ) {
                        Text(
                            text = "LOAD DEMO DATA",
                            style = TesseraType.TelemetryBadge,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
