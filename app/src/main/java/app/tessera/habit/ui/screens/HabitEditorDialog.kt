package app.tessera.habit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.components.SectionHeading
import app.tessera.habit.ui.components.TesseraChip
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraDivider
import app.tessera.habit.ui.components.TesseraIconButton
import app.tessera.habit.ui.components.TesseraPrimaryButton
import app.tessera.habit.ui.components.TesseraSecondaryButton
import app.tessera.habit.ui.components.tesseraTextFieldColors
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.HabitDraft
import java.util.Locale

private val categorySuggestions = listOf("Health", "Mind", "Work", "Home")

private const val MIN_TARGET_PER_DAY = 1
private const val MAX_TARGET_PER_DAY = 24
private const val ICONS_PER_ROW = 6

/**
 * The create/edit habit form. Closing the sheet ([onDismiss]) keeps the draft in memory;
 * only [onDiscardDraft] or a successful save clears it.
 */
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

    var whyExpanded by rememberSaveable {
        mutableStateOf(
            draft.identityStatement.isNotBlank() ||
                draft.cueAnchor.isNotBlank() ||
                draft.microStarter.isNotBlank() ||
                draft.temptationReward.isNotBlank()
        )
    }
    var automationExpanded by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = TesseraDimens.RadiusCard,
            topEnd = TesseraDimens.RadiusCard
        ),
        containerColor = colors.surfaceCard,
        contentColor = colors.textPrimary,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header stays put while the form scrolls.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TesseraDimens.ScreenPadding,
                        end = TesseraDimens.SpaceS,
                        top = TesseraDimens.SpaceM,
                        bottom = TesseraDimens.SpaceXs
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit habit" else "New habit",
                    style = TesseraType.Headline,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                TesseraIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    filled = false
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = TesseraDimens.ScreenPadding,
                        vertical = TesseraDimens.SpaceM
                    ),
                verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceXl)
            ) {
                EditorTextField(
                    value = draft.title,
                    onValueChange = { newTitle ->
                        val nextSlug = when {
                            draft.slugManuallyEdited -> draft.slug
                            newTitle.isBlank() -> ""
                            else -> HabitEntity.generateSlug(newTitle)
                        }
                        onUpdateDraft(draft.copy(title = newTitle, slug = nextSlug))
                    },
                    label = "Name",
                    placeholder = "Write 500 words"
                )

                Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                    SectionHeading(text = "Times per day")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceL)
                    ) {
                        TargetStepper(
                            value = draft.targetPerDay,
                            onValueChange = { onUpdateDraft(draft.copy(targetPerDay = it)) }
                        )
                        EditorTextField(
                            value = draft.unitLabel,
                            onValueChange = { onUpdateDraft(draft.copy(unitLabel = it)) },
                            label = "Unit",
                            placeholder = "times, glasses",
                            capitalization = KeyboardCapitalization.None,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "Most habits are once a day.",
                        style = TesseraType.Supporting,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = TesseraDimens.SpaceXs)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                    EditorTextField(
                        value = draft.category,
                        onValueChange = { onUpdateDraft(draft.copy(category = it)) },
                        label = "Category (optional)",
                        placeholder = "Health"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)
                    ) {
                        categorySuggestions.forEach { suggestion ->
                            TesseraChip(
                                text = suggestion,
                                selected = draft.category.trim().equals(suggestion, ignoreCase = true),
                                onClick = { onUpdateDraft(draft.copy(category = suggestion)) },
                                modifier = Modifier.heightIn(min = TesseraDimens.TouchTarget)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
                    SectionHeading(text = "Icon")
                    IconPicker(
                        selectedKey = draft.iconKey,
                        onSelect = { onUpdateDraft(draft.copy(iconKey = it)) }
                    )
                }

                EditorTextField(
                    value = draft.description,
                    onValueChange = { onUpdateDraft(draft.copy(description = it)) },
                    label = "Notes (optional)",
                    placeholder = "Anything worth remembering about this habit",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 5
                )

                Column {
                    TesseraDivider()
                    CollapsibleHeader(
                        title = "Why and how (optional)",
                        expanded = whyExpanded,
                        onToggle = { whyExpanded = !whyExpanded }
                    )
                    if (whyExpanded) {
                        Column(
                            modifier = Modifier.padding(bottom = TesseraDimens.SpaceL),
                            verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceM)
                        ) {
                            EditorTextField(
                                value = draft.identityStatement,
                                onValueChange = { onUpdateDraft(draft.copy(identityStatement = it)) },
                                label = "Who this makes me",
                                placeholder = "I'm someone who writes every day",
                                singleLine = false,
                                maxLines = 3
                            )
                            EditorTextField(
                                value = draft.cueAnchor,
                                onValueChange = { onUpdateDraft(draft.copy(cueAnchor = it)) },
                                label = "When I'll do it",
                                placeholder = "After morning coffee",
                                singleLine = false,
                                maxLines = 3
                            )
                            EditorTextField(
                                value = draft.microStarter,
                                onValueChange = { onUpdateDraft(draft.copy(microStarter = it)) },
                                label = "Two-minute version",
                                placeholder = "Write one sentence",
                                singleLine = false,
                                maxLines = 3
                            )
                            EditorTextField(
                                value = draft.temptationReward,
                                onValueChange = { onUpdateDraft(draft.copy(temptationReward = it)) },
                                label = "Reward",
                                placeholder = "Tea on the balcony",
                                singleLine = false,
                                maxLines = 3
                            )
                        }
                    }

                    TesseraDivider()
                    CollapsibleHeader(
                        title = "Automation",
                        expanded = automationExpanded,
                        onToggle = { automationExpanded = !automationExpanded }
                    )
                    if (automationExpanded) {
                        EditorTextField(
                            value = draft.slug,
                            onValueChange = { raw ->
                                val cleaned = raw
                                    .lowercase(Locale.US)
                                    .replace(Regex("[^a-z0-9-]+"), "-")
                                onUpdateDraft(draft.copy(slug = cleaned, slugManuallyEdited = true))
                            },
                            label = "Automation ID",
                            placeholder = "write-500-words",
                            supportingText = "Used by Tasker, Routines and deep links to log this habit.",
                            textStyle = TesseraType.Code,
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Ascii
                        )
                    }
                }
            }

            // Actions stay visible above the keyboard.
            Column(modifier = Modifier.fillMaxWidth()) {
                TesseraDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = TesseraDimens.ScreenPadding,
                            vertical = TesseraDimens.SpaceM
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceM)
                ) {
                    if (draft.hasUnsavedChanges) {
                        TesseraSecondaryButton(
                            text = "Discard",
                            onClick = onDiscardDraft
                        )
                    }
                    TesseraPrimaryButton(
                        text = if (isEditing) "Save changes" else "Save habit",
                        onClick = { onSaveHabit(draft.toHabitEntity()) },
                        enabled = draft.title.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    textStyle: TextStyle = TesseraType.Body,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val supportingContent: (@Composable () -> Unit)? = if (supportingText != null) {
        { Text(text = supportingText, style = TesseraType.Supporting) }
    } else {
        null
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = textStyle,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder, style = textStyle) },
        supportingText = supportingContent,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType
        ),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        maxLines = if (singleLine) 1 else maxLines.coerceAtLeast(minLines),
        shape = RoundedCornerShape(TesseraDimens.RadiusControl),
        colors = tesseraTextFieldColors()
    )
}

@Composable
private fun TargetStepper(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val colors = TesseraTheme.colors
    val canDecrease = value > MIN_TARGET_PER_DAY
    val canIncrease = value < MAX_TARGET_PER_DAY
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)
    ) {
        TesseraIconButton(
            icon = Icons.Rounded.Remove,
            contentDescription = "One fewer per day",
            onClick = { if (canDecrease) onValueChange(value - 1) },
            modifier = Modifier.background(colors.surfaceElevated, CircleShape),
            filled = false,
            tint = if (canDecrease) colors.textPrimary else colors.textSecondary.copy(alpha = 0.4f)
        )
        Text(
            text = value.toString(),
            style = TesseraType.Title,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = TesseraDimens.SpaceXxl)
        )
        TesseraIconButton(
            icon = Icons.Rounded.Add,
            contentDescription = "One more per day",
            onClick = { if (canIncrease) onValueChange(value + 1) },
            modifier = Modifier.background(colors.surfaceElevated, CircleShape),
            filled = false,
            tint = if (canIncrease) colors.textPrimary else colors.textSecondary.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun IconPicker(
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    val colors = TesseraTheme.colors
    val shape = RoundedCornerShape(TesseraDimens.RadiusControl)
    // Compare resolved icons so a stored alias key still shows its icon as selected.
    val selectedIcon = HabitIcons.resolve(selectedKey)
    Column(verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)) {
        HabitIcons.allIcons.chunked(ICONS_PER_ROW).forEach { rowIcons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)
            ) {
                rowIcons.forEach { option ->
                    val isSelected = option.icon == selectedIcon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = TesseraDimens.TouchTarget)
                            .clip(shape)
                            .background(if (isSelected) colors.accent else colors.surfaceElevated)
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSelect(option.key) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.label,
                            tint = if (isSelected) colors.onAccent else colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                // Keep cells in a short last row the same width as the rows above.
                repeat(ICONS_PER_ROW - rowIcons.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CollapsibleHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = TesseraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TesseraDimens.TouchTarget)
            .clickable(
                onClickLabel = if (expanded) "Collapse" else "Expand",
                role = Role.Button,
                onClick = onToggle
            )
            .padding(horizontal = TesseraDimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TesseraType.BodyStrong,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
