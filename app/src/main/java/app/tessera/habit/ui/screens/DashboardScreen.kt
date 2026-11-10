package app.tessera.habit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitCard
import app.tessera.habit.ui.components.HabitCompactCard
import app.tessera.habit.ui.components.TesseraCard
import app.tessera.habit.ui.components.TesseraChip
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraIconButton
import app.tessera.habit.ui.components.TesseraPrimaryButton
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private const val CATEGORY_ALL = "ALL"
private const val CATEGORY_ARCHIVED = "ARCHIVED"

/** The Today tab: date, today's progress, and one card per habit. */
@Composable
fun DashboardScreen(
    uiState: TesseraUiState,
    onCheckInHabit: (Long) -> Unit,
    onOpenHabitDetail: (Long) -> Unit,
    onOpenCreateHabit: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val today = LocalDate.now()

    val totalActive = uiState.activeHabits.size
    val doneToday = remember(uiState.activeHabits, today) {
        uiState.activeHabits.count { it.isCompletedOn(today) }
    }
    val isArchivedFilter = uiState.selectedCategory.equals(CATEGORY_ARCHIVED, ignoreCase = true)
    val namedCategoryCount = uiState.categories.count {
        !it.equals(CATEGORY_ALL, ignoreCase = true) && !it.equals(CATEGORY_ARCHIVED, ignoreCase = true)
    }
    val showCategoryChips = namedCategoryCount > 1 ||
        uiState.archivedHabits.isNotEmpty() ||
        !uiState.selectedCategory.equals(CATEGORY_ALL, ignoreCase = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss),
        contentPadding = PaddingValues(
            start = TesseraDimens.ScreenPadding,
            end = TesseraDimens.ScreenPadding,
            top = TesseraDimens.SpaceL,
            bottom = TesseraDimens.SpaceXxl
        ),
        verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceM)
    ) {
        item(key = "header") {
            TodayHeader(
                today = today,
                doneToday = doneToday,
                totalActive = totalActive,
                isCompactView = uiState.isCompactBentoView,
                onToggleViewMode = onToggleViewMode,
                onOpenCreateHabit = onOpenCreateHabit
            )
        }

        if (totalActive > 0) {
            item(key = "horizon") {
                ProgressHorizon(doneToday = doneToday, totalActive = totalActive)
            }
        }

        if (showCategoryChips) {
            item(key = "categories") {
                CategoryChips(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = onSelectCategory
                )
            }
        }

        if (uiState.filteredHabits.isEmpty()) {
            item(key = "empty") {
                EmptyHabitsCard(
                    isArchivedFilter = isArchivedFilter,
                    onOpenCreateHabit = onOpenCreateHabit
                )
            }
        } else if (uiState.isCompactBentoView) {
            items(
                items = uiState.filteredHabits.chunked(2),
                key = { pair -> "row_${pair.first().habit.id}" }
            ) { rowHabits ->
                CompactRow(
                    rowHabits = rowHabits,
                    today = today,
                    onCheckInHabit = onCheckInHabit,
                    onOpenHabitDetail = onOpenHabitDetail
                )
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
private fun TodayHeader(
    today: LocalDate,
    doneToday: Int,
    totalActive: Int,
    isCompactView: Boolean,
    onToggleViewMode: () -> Unit,
    onOpenCreateHabit: () -> Unit
) {
    val colors = TesseraTheme.colors
    val weekday = remember(today) {
        today.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.US)
    }
    val dateText = remember(today) {
        today.format(DateTimeFormatter.ofPattern("d MMMM", Locale.US))
    }
    val summary = if (totalActive > 0) "$dateText · $doneToday of $totalActive done" else dateText

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = weekday,
                style = TesseraType.Headline,
                color = colors.textPrimary
            )
            Text(
                text = summary,
                style = TesseraType.Body,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(TesseraDimens.SpaceS))
        TesseraIconButton(
            icon = if (isCompactView) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
            contentDescription = if (isCompactView) "Show as list" else "Show as grid",
            onClick = onToggleViewMode
        )
        Spacer(modifier = Modifier.width(TesseraDimens.SpaceS))
        TesseraIconButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Add a habit",
            onClick = onOpenCreateHabit
        )
    }
}

/**
 * Today's progress as a horizon: a line across the page with a sun that sits at
 * done / total of the way along, and a smaller second sun trailing behind it.
 */
@Composable
private fun ProgressHorizon(
    doneToday: Int,
    totalActive: Int
) {
    val colors = TesseraTheme.colors
    val fraction = if (totalActive > 0) {
        (doneToday.toFloat() / totalActive.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedFraction by animateFloatAsState(targetValue = fraction, label = "horizonSun")

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val sunRadius = 8.dp.toPx()
        val smallSunRadius = 4.dp.toPx()
        val glowRadius = 28.dp.toPx()
        val lineWidth = 2.dp.toPx()
        val horizonY = size.height * 0.6f
        val sunX = sunRadius + (size.width - sunRadius * 2f) * animatedFraction
        val sunCentre = Offset(sunX, horizonY)

        drawLine(
            color = colors.borderHairline,
            start = Offset(0f, horizonY),
            end = Offset(size.width, horizonY),
            strokeWidth = lineWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = colors.accent,
            start = Offset(0f, horizonY),
            end = Offset(sunX, horizonY),
            strokeWidth = lineWidth,
            cap = StrokeCap.Round
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.accent.copy(alpha = 0.28f), colors.accent.copy(alpha = 0f)),
                center = sunCentre,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = sunCentre
        )

        // The second sun trails behind and above; it is hidden until there is room for it.
        val smallSunX = sunX - 26.dp.toPx()
        if (smallSunX >= smallSunRadius) {
            drawCircle(
                color = colors.accent.copy(alpha = 0.55f),
                radius = smallSunRadius,
                center = Offset(smallSunX, horizonY - 10.dp.toPx())
            )
        }

        drawCircle(color = colors.accent, radius = sunRadius, center = sunCentre)
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            // "ALL" and "ARCHIVED" are the keys the view model filters on; only the label changes.
            val label = when {
                category.equals(CATEGORY_ALL, ignoreCase = true) -> "All"
                category.equals(CATEGORY_ARCHIVED, ignoreCase = true) -> "Archived"
                else -> category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
                }
            }
            TesseraChip(
                text = label,
                selected = category.equals(selectedCategory, ignoreCase = true),
                onClick = { onSelectCategory(category) },
                modifier = Modifier.heightIn(min = TesseraDimens.TouchTarget)
            )
        }
    }
}

@Composable
private fun CompactRow(
    rowHabits: List<HabitWithLogs>,
    today: LocalDate,
    onCheckInHabit: (Long) -> Unit,
    onOpenHabitDetail: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceM)
    ) {
        rowHabits.forEach { item ->
            HabitCompactCard(
                habitWithLogs = item,
                onCheckInClick = { onCheckInHabit(item.habit.id) },
                onCardClick = { onOpenHabitDetail(item.habit.id) },
                today = today,
                modifier = Modifier.weight(1f)
            )
        }
        if (rowHabits.size < 2) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyHabitsCard(
    isArchivedFilter: Boolean,
    onOpenCreateHabit: () -> Unit
) {
    val colors = TesseraTheme.colors
    TesseraCard(contentPadding = TesseraDimens.SpaceXl) {
        Text(
            text = if (isArchivedFilter) "No archived habits" else "Nothing here yet",
            style = TesseraType.Headline,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
        Text(
            text = if (isArchivedFilter) {
                "Habits you archive are kept here with their history."
            } else {
                "Add a habit and check it off each day. Every day you do it fills a square."
            },
            style = TesseraType.Body,
            color = colors.textSecondary
        )
        if (!isArchivedFilter) {
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceXl))
            TesseraPrimaryButton(
                text = "Add your first habit",
                onClick = onOpenCreateHabit,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Add
            )
        }
    }
}
