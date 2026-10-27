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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.HabitDraft
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorDialog(
    draft: HabitDraft,
    isEditing: Boolean,
    onUpdateDraft: (HabitDraft) -> Unit,
    onDiscardDraft: () -> Unit,
    onDismiss: () -> Unit,
    onSaveHabit: (HabitEntity) -> Unit
) {
    val colors = TesseraTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedPalette = remember(draft.colorKey) { HabitPalette.resolve(draft.colorKey) }
    val effectiveSlug = draft.slug.ifBlank {
        HabitEntity.generateSlug(draft.title.ifBlank { "new-ritual" })
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
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with In-Memory Draft Persistence Indicator & Discard Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isEditing) "HABIT ARCHITECTURE" else "ATOMIC HABIT BUILDER",
                            style = TesseraType.MicroAxis,
                            color = selectedPalette.colorForLevel(4, colors.isDark)
                        )
                        if (!isEditing && draft.hasUnsavedChanges) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.surfaceElevated)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DRAFT PRESERVED",
                                    style = TesseraType.MicroAxis,
                                    color = selectedPalette.colorForLevel(3, colors.isDark)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isEditing) "Edit ${draft.title.ifBlank { "Ritual" }}" else "Synthesize New Ritual",
                        style = TesseraType.SectionTitle,
                        color = colors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (draft.hasUnsavedChanges || isEditing) {
                        TextButton(onClick = onDiscardDraft) {
                            Text(
                                text = if (isEditing) "Reset" else "Clear Draft",
                                style = TesseraType.MicroAxis,
                                color = colors.textMuted
                            )
                        }
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(selectedPalette.colorForLevel(1, colors.isDark))
                                .border(1.dp, selectedPalette.level3.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HabitIcons.resolve(draft.iconKey),
                                contentDescription = null,
                                tint = selectedPalette.colorForLevel(4, colors.isDark),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = draft.title.ifBlank { "Untitled Ritual" },
                                style = TesseraType.CardTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${draft.category.uppercase(Locale.US)} · #$effectiveSlug · ${draft.targetPerDay}x/day",
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

            // 2. ATOMIC HABITS: Law 4 — Identity Anchor ("Who do you want to become?")
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = selectedPalette.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LAW 4: IDENTITY ANCHOR (MAKE IT SATISFYING)",
                            style = TesseraType.MicroAxis,
                            color = selectedPalette.colorForLevel(4, colors.isDark)
                        )
                    }
                    Text(
                        text = "Every check-in is a vote for the type of person you wish to become. Re-frame the habit as your identity.",
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = draft.identityStatement,
                        onValueChange = { onUpdateDraft(draft.copy(identityStatement = it)) },
                        label = { Text("Identity Statement (\"I am the type of person who...\")", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("e.g. I am a writer who publishes daily, or I am an athlete who honors recovery", style = TesseraType.CardSubtitle) },
                        singleLine = false,
                        maxLines = 2,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. ATOMIC HABITS: Law 1 — Cue & Habit Stacking ("Make It Obvious")
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = selectedPalette.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LAW 1: THE CUE & HABIT STACKING (MAKE IT OBVIOUS)",
                            style = TesseraType.MicroAxis,
                            color = selectedPalette.colorForLevel(4, colors.isDark)
                        )
                    }

                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { newTitle ->
                            val updatedSlug = if (!draft.slugManuallyEdited) {
                                HabitEntity.generateSlug(newTitle)
                            } else draft.slug
                            onUpdateDraft(draft.copy(title = newTitle, slug = updatedSlug))
                        },
                        label = { Text("Habit Behavior / Action", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("e.g. Write 500 Words, Deep Architecture Work, Vocal Scales", style = TesseraType.CardSubtitle) },
                        singleLine = true,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = draft.cueAnchor,
                        onValueChange = { onUpdateDraft(draft.copy(cueAnchor = it)) },
                        label = { Text("Anchor Event (\"After I...\")", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("e.g. After I brew morning coffee, or After I step out of shower...", style = TesseraType.CardSubtitle) },
                        singleLine = false,
                        maxLines = 2,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. ATOMIC HABITS: Law 3 — The 2-Minute Gateway Rule ("Make It Easy")
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = selectedPalette.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LAW 3: THE 2-MINUTE RULE (MAKE IT EASY)",
                            style = TesseraType.MicroAxis,
                            color = selectedPalette.colorForLevel(4, colors.isDark)
                        )
                    }
                    Text(
                        text = "Scale down your habit until it takes under 120 seconds. Master the art of showing up before optimizing perfection.",
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = draft.microStarter,
                        onValueChange = { onUpdateDraft(draft.copy(microStarter = it)) },
                        label = { Text("2-Minute Gateway Starter (\"To start, I will...\")", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("e.g. Write 1 single sentence, or Tie gym sneakers and step onto mat", style = TesseraType.CardSubtitle) },
                        singleLine = false,
                        maxLines = 2,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 5. ATOMIC HABITS: Law 2 — Temptation Bundling ("Make It Attractive")
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = selectedPalette.colorForLevel(4, colors.isDark),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LAW 2: TEMPTATION BUNDLING (MAKE IT ATTRACTIVE)",
                            style = TesseraType.MicroAxis,
                            color = selectedPalette.colorForLevel(4, colors.isDark)
                        )
                    }
                    Text(
                        text = "Pair an action you NEED to do with an action you WANT to do. Reward anticipation releases dopamine.",
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = draft.temptationReward,
                        onValueChange = { onUpdateDraft(draft.copy(temptationReward = it)) },
                        label = { Text("Temptation Bundle Reward (\"Then I get to...\")", style = TesseraType.CardSubtitle) },
                        placeholder = { Text("e.g. Listen to my favourite podcast, drink artisanal espresso, browse tech news", style = TesseraType.CardSubtitle) },
                        singleLine = false,
                        maxLines = 2,
                        colors = editorTextFieldColors(selectedPalette.level3),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 6. Category Selector
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
                        val active = draft.category.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) selectedPalette.colorForLevel(4, colors.isDark) else colors.surfaceCard)
                                .border(
                                    1.dp,
                                    if (active) selectedPalette.colorForLevel(4, colors.isDark) else colors.borderHairline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onUpdateDraft(draft.copy(category = cat)) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat.uppercase(Locale.US),
                                style = TesseraType.TelemetryBadge,
                                color = if (active) selectedPalette.onAccentFor(colors.isDark) else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // 8. Quantitative Target & Weekly Cadence
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
                                text = if (draft.targetPerDay <= 1) "Binary (Done / Not Done)" else "Multi-Shade (${draft.targetPerDay} ${draft.unitLabel}/day)",
                                style = TesseraType.CardSubtitle,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StepperIconButton(Icons.Rounded.Remove) {
                                onUpdateDraft(draft.copy(targetPerDay = (draft.targetPerDay - 1).coerceAtLeast(1)))
                            }
                            Text(
                                text = "${draft.targetPerDay}x",
                                style = TesseraType.TelemetryMedium,
                                color = selectedPalette.colorForLevel(4, colors.isDark)
                            )
                            StepperIconButton(Icons.Rounded.Add) {
                                onUpdateDraft(draft.copy(targetPerDay = (draft.targetPerDay + 1).coerceAtMost(24)))
                            }
                        }
                    }

                    if (draft.targetPerDay > 1) {
                        OutlinedTextField(
                            value = draft.unitLabel,
                            onValueChange = { onUpdateDraft(draft.copy(unitLabel = it)) },
                            label = { Text("Unit Label (e.g. sessions, pomodoros, glasses, pages)", style = TesseraType.CardSubtitle) },
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
                                text = "${draft.weeklyGoalDays} days per week",
                                style = TesseraType.CardSubtitle,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StepperIconButton(Icons.Rounded.Remove) {
                                onUpdateDraft(draft.copy(weeklyGoalDays = (draft.weeklyGoalDays - 1).coerceAtLeast(1)))
                            }
                            Text(
                                text = "${draft.weeklyGoalDays}D/WK",
                                style = TesseraType.TelemetryBadge,
                                color = colors.textPrimary
                            )
                            StepperIconButton(Icons.Rounded.Add) {
                                onUpdateDraft(draft.copy(weeklyGoalDays = (draft.weeklyGoalDays + 1).coerceAtMost(7)))
                            }
                        }
                    }
                }
            }

            // 9. 8-Palette 5-Stop Chromatic Swatch Picker
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
                                    .clickable { onUpdateDraft(draft.copy(colorKey = spec.key)) },
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

            // 10. 24-Icon Minimalist Vector Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GEOMETRIC ICON GLYPH", style = TesseraType.MicroAxis, color = colors.textMuted)
                val iconRows = HabitIcons.allIcons.chunked(6)
                iconRows.forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowIcons.forEach { option ->
                            val isSelected = option.key == draft.iconKey
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
                                    .clickable { onUpdateDraft(draft.copy(iconKey = option.key)) },
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

            // 11. External API Slug (#)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("EXTERNAL API / INTENT SLUG (#)", style = TesseraType.MicroAxis, color = colors.textMuted)
                OutlinedTextField(
                    value = draft.slug,
                    onValueChange = { raw ->
                        val cleaned = raw.lowercase(Locale.US).replace(Regex("[^a-z0-9-]+"), "-")
                        onUpdateDraft(draft.copy(slug = cleaned, slugManuallyEdited = true))
                    },
                    placeholder = { Text("e.g. deep-work", style = TesseraType.CodeSnippet) },
                    textStyle = TesseraType.CodeSnippet.copy(color = colors.textPrimary),
                    singleLine = true,
                    colors = editorTextFieldColors(selectedPalette.level3),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 12. Save Button
            Button(
                onClick = { onSaveHabit(draft.toHabitEntity()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedPalette.colorForLevel(4, colors.isDark),
                    contentColor = selectedPalette.onAccentFor(colors.isDark)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isEditing) "SAVE RITUAL CHANGES" else "FORGE RITUAL (#$effectiveSlug)",
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
        focusedTextColor = TesseraTheme.colors.textPrimary,
        unfocusedTextColor = TesseraTheme.colors.textPrimary,
        cursorColor = accentBorder,
        focusedBorderColor = accentBorder,
        unfocusedBorderColor = TesseraTheme.colors.borderSpecular,
        focusedContainerColor = TesseraTheme.colors.surfaceAbyss,
        unfocusedContainerColor = TesseraTheme.colors.surfaceAbyss,
        focusedLabelColor = accentBorder,
        unfocusedLabelColor = TesseraTheme.colors.textSecondary,
        focusedPlaceholderColor = TesseraTheme.colors.textMuted,
        unfocusedPlaceholderColor = TesseraTheme.colors.textMuted
    )
