package app.tessera.habit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorDialog(
    initialHabit: HabitEntity?,
    onDismiss: () -> Unit,
    onSaveHabit: (HabitEntity) -> Unit
) {
    val colors = TesseraTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = initialHabit != null && initialHabit.id > 0L

    var title by remember(initialHabit) { mutableStateOf(initialHabit?.title.orEmpty()) }
    var slug by remember(initialHabit) { mutableStateOf(initialHabit?.slug.orEmpty()) }
    var slugManuallyEdited by remember(initialHabit) { mutableStateOf(isEditing) }
    var description by remember(initialHabit) { mutableStateOf(initialHabit?.description.orEmpty()) }
    var category by remember(initialHabit) { mutableStateOf(initialHabit?.category ?: "Focus") }
    var iconKey by remember(initialHabit) { mutableStateOf(initialHabit?.iconKey ?: "bolt") }
    var colorKey by remember(initialHabit) { mutableStateOf(initialHabit?.colorKey ?: "emerald") }
    var targetPerDay by remember(initialHabit) { mutableIntStateOf(initialHabit?.targetPerDay ?: 1) }
    var unitLabel by remember(initialHabit) { mutableStateOf(initialHabit?.unitLabel ?: "sessions") }
    var weeklyGoalDays by remember(initialHabit) { mutableIntStateOf(initialHabit?.weeklyGoalDays ?: 7) }

    val selectedPalette = remember(colorKey) { HabitPalette.resolve(colorKey) }
    val effectiveSlug = slug.ifBlank {
        HabitEntity.generateSlug(title.ifBlank { "new-ritual" })
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditing) "CONFIGURE RITUAL" else "ARCHITECT NEW RITUAL",
                        style = TesseraType.MicroAxis,
                        color = selectedPalette.colorForLevel(4, colors.isDark)
                    )
                    Text(
                        text = if (isEditing) "Edit ${initialHabit?.title}" else "Create Daily Ritual",
                        style = TesseraType.SectionTitle,
                        color = colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.surfaceCard)
                        .border(1.dp, colors.borderHairline, RoundedCornerShape(9.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 1. Live Architectural Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, selectedPalette.level3.copy(alpha = 0.55f))
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(selectedPalette.colorForLevel(1, colors.isDark))
                                .border(1.dp, selectedPalette.level3.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HabitIcons.resolve(iconKey),
                                contentDescription = null,
                                tint = selectedPalette.colorForLevel(4, colors.isDark),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title.ifBlank { "Untitled Ritual" },
                                style = TesseraType.CardTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${category.uppercase(Locale.US)} · #$effectiveSlug · ${targetPerDay}x/day",
                                style = TesseraType.MicroAxis,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // 5-stop mini swatch preview
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (lvl in 0..4) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(selectedPalette.colorForLevel(lvl, colors.isDark))
                            )
                        }
                    }
                }
            }

            // 2. Identity Inputs (Title, Programmatic Slug, Description)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("IDENTITY & API SLUG", style = TesseraType.MicroAxis, color = colors.textMuted)

                OutlinedTextField(
                    value = title,
                    onValueChange = { newTitle ->
                        title = newTitle
                        if (!slugManuallyEdited) {
                            slug = HabitEntity.generateSlug(newTitle)
                        }
                    },
                    label = { Text("Ritual Title", style = TesseraType.CardSubtitle) },
                    placeholder = { Text("e.g., Deep Work Session", style = TesseraType.CardSubtitle) },
                    singleLine = true,
                    colors = editorTextFieldColors(selectedPalette.level3),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = slug,
                        onValueChange = { raw ->
                            slugManuallyEdited = true
                            slug = raw.lowercase(Locale.US).replace(Regex("[^a-z0-9-]+"), "-")
                        },
                        label = { Text("Programmatic Slug (#)", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("deep-work", style = TesseraType.CodeSnippet) },
                        textStyle = TesseraType.CodeSnippet.copy(color = colors.textPrimary),
                        singleLine = true,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Cue / Subtitle", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("Before 10 AM", style = TesseraType.CardSubtitle) },
                        singleLine = true,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Category Pill Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CATEGORY TAG", style = TesseraType.MicroAxis, color = colors.textMuted)
                val presetCategories = listOf("Focus", "Fitness", "Mind", "Health", "Craft", "Finance")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetCategories.forEach { cat ->
                        val active = category.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) selectedPalette.colorForLevel(4, colors.isDark) else colors.surfaceCard)
                                .border(
                                    1.dp,
                                    if (active) selectedPalette.colorForLevel(4, colors.isDark) else colors.borderHairline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat.uppercase(Locale.US),
                                style = TesseraType.TelemetryBadge,
                                color = if (active) selectedPalette.onAccent else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // 4. Quantitative Target & Weekly Cadence
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DAILY TARGET INTENSITY", style = TesseraType.MicroAxis, color = colors.textMuted)
                            Text(
                                text = if (targetPerDay <= 1) "Binary (Done / Not Done)" else "Multi-Shade ($targetPerDay $unitLabel/day)",
                                style = TesseraType.CardSubtitle,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StepperIconButton(Icons.Rounded.Remove) {
                                targetPerDay = (targetPerDay - 1).coerceAtLeast(1)
                            }
                            Text(
                                text = "${targetPerDay}x",
                                style = TesseraType.TelemetryMedium,
                                color = selectedPalette.colorForLevel(4, colors.isDark)
                            )
                            StepperIconButton(Icons.Rounded.Add) {
                                targetPerDay = (targetPerDay + 1).coerceAtMost(24)
                            }
                        }
                    }

                    if (targetPerDay > 1) {
                        OutlinedTextField(
                            value = unitLabel,
                            onValueChange = { unitLabel = it },
                            label = { Text("Unit Label (e.g., pomodoros, glasses, pages, sets)", style = TesseraType.CardSubtitle) },
                            singleLine = true,
                            colors = editorTextFieldColors(selectedPalette.level3),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("WEEKLY GOAL CADENCE", style = TesseraType.MicroAxis, color = colors.textMuted)
                            Text(
                                text = "$weeklyGoalDays days per week",
                                style = TesseraType.CardSubtitle,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StepperIconButton(Icons.Rounded.Remove) {
                                weeklyGoalDays = (weeklyGoalDays - 1).coerceAtLeast(1)
                            }
                            Text(
                                text = "${weeklyGoalDays}D/WK",
                                style = TesseraType.TelemetryBadge,
                                color = colors.textPrimary
                            )
                            StepperIconButton(Icons.Rounded.Add) {
                                weeklyGoalDays = (weeklyGoalDays + 1).coerceAtMost(7)
                            }
                        }
                    }
                }
            }

            // 5. 8-Palette 5-Stop Chromatic Swatch Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("5-STOP CHROMATIC PALETTE", style = TesseraType.MicroAxis, color = colors.textMuted)
                val paletteRows = HabitPalette.allPalettes.chunked(2)
                paletteRows.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { spec ->
                            val isSelected = spec.key == selectedPalette.key
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { colorKey = spec.key },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceCard,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) spec.colorForLevel(4, colors.isDark) else colors.borderHairline
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = spec.displayName,
                                            style = TesseraType.CardSubtitle,
                                            color = colors.textPrimary
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = spec.colorForLevel(4, colors.isDark),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        for (lvl in 0..4) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(spec.colorForLevel(lvl, colors.isDark))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. 24-Icon Minimalist Vector Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GEOMETRIC ICON GLYPH", style = TesseraType.MicroAxis, color = colors.textMuted)
                val iconRows = HabitIcons.allIcons.chunked(6)
                iconRows.forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowIcons.forEach { option ->
                            val isSelected = option.key == iconKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) selectedPalette.colorForLevel(1, colors.isDark)
                                        else colors.surfaceCard
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) selectedPalette.colorForLevel(4, colors.isDark)
                                        else colors.borderHairline,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { iconKey = option.key },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.label,
                                    tint = if (isSelected) selectedPalette.colorForLevel(4, colors.isDark)
                                    else colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    val finalTitle = title.trim().ifEmpty { "Daily Ritual" }
                    val finalSlug = slug.trim().ifEmpty { HabitEntity.generateSlug(finalTitle) }
                    onSaveHabit(
                        HabitEntity(
                            id = initialHabit?.id ?: 0L,
                            slug = finalSlug,
                            title = finalTitle,
                            description = description.trim(),
                            category = category.trim().ifEmpty { "Focus" },
                            iconKey = iconKey,
                            colorKey = colorKey,
                            unitLabel = unitLabel.trim().ifEmpty { "times" },
                            targetPerDay = targetPerDay.coerceIn(1, 24),
                            weeklyGoalDays = weeklyGoalDays.coerceIn(1, 7),
                            isArchived = initialHabit?.isArchived ?: false,
                            sortOrder = initialHabit?.sortOrder ?: 0,
                            createdAt = initialHabit?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedPalette.colorForLevel(4, colors.isDark),
                    contentColor = selectedPalette.onAccent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isEditing) "SAVE RITUAL CHANGES" else "CREATE RITUAL (#$effectiveSlug)",
                    style = TesseraType.TelemetryMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StepperIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun editorTextFieldColors(accentBorder: androidx.compose.ui.graphics.Color) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentBorder,
        unfocusedBorderColor = TesseraTheme.colors.borderHairline,
        focusedContainerColor = TesseraTheme.colors.surfaceCard,
        unfocusedContainerColor = TesseraTheme.colors.surfaceCard,
        focusedLabelColor = accentBorder,
        unfocusedLabelColor = TesseraTheme.colors.textSecondary
    )
