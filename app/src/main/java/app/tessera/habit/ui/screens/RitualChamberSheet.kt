package app.tessera.habit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualChamberSheet(
    activeHabits: List<HabitWithLogs>,
    activeRitualTime: String,
    onSelectRitualTime: (String) -> Unit,
    onCheckInHabit: (Long) -> Unit,
    onOpenCreateHabit: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = TesseraTheme.colors
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val trackHabits = remember(activeHabits, activeRitualTime) {
        val filtered = activeHabits.filter {
            it.habit.ritualTime.equals(activeRitualTime, ignoreCase = true)
        }
        if (filtered.isNotEmpty()) filtered else activeHabits
    }

    val pendingHabits = remember(trackHabits, today) {
        trackHabits.filter { it.countForDate(today) < it.habit.targetPerDay }
    }

    val completedCount = trackHabits.size - pendingHabits.size
    val totalCount = trackHabits.size

    var currentStepIndex by remember(activeRitualTime, pendingHabits.size) {
        mutableIntStateOf(0)
    }

    val activeItem = pendingHabits.getOrNull(currentStepIndex)
    val isAllCompleted = trackHabits.isNotEmpty() && pendingHabits.isEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceAbyss,
        contentColor = colors.textPrimary,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
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
                        text = "FABULOUS GUIDED RITUAL CHAMBER",
                        style = TesseraType.MicroAxis,
                        color = HabitPalette.AmberWarm.colorForLevel(4, colors.isDark)
                    )
                    Text(
                        text = when (activeRitualTime) {
                            "DAWN" -> "Dawn Ritual"
                            "FOCUS" -> "Focus Ritual"
                            "TWILIGHT" -> "Twilight Ritual"
                            else -> "Guided Routine"
                        },
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

            // Ritual Track Selector Pills (Dawn, Focus, Twilight)
            val ritualTracks = listOf(
                Triple("DAWN", "Dawn", Icons.Rounded.WbSunny),
                Triple("FOCUS", "Focus", Icons.Rounded.Bolt),
                Triple("TWILIGHT", "Twilight", Icons.Rounded.NightsStay)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ritualTracks.forEach { (trackKey, trackLabel, trackIcon) ->
                    val isSelected = activeRitualTime.equals(trackKey, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.surfaceElevated else colors.surfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) HabitPalette.AmberWarm.colorForLevel(4, colors.isDark) else colors.borderHairline,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectRitualTime(trackKey)
                                currentStepIndex = 0
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = trackIcon,
                                contentDescription = null,
                                tint = if (isSelected) HabitPalette.AmberWarm.colorForLevel(4, colors.isDark) else colors.textMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = trackLabel.uppercase(),
                                style = TesseraType.TelemetryBadge,
                                color = if (isSelected) colors.textPrimary else colors.textMuted
                            )
                        }
                    }
                }
            }

            // Progress Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceCard,
                border = BorderStroke(1.dp, colors.borderHairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RITUAL PROGRESSION",
                            style = TesseraType.MicroAxis,
                            color = colors.textMuted
                        )
                        Text(
                            text = if (trackHabits.isEmpty()) "No habits assigned to this track"
                            else "$completedCount of $totalCount steps completed",
                            style = TesseraType.CardSubtitle,
                            color = colors.textPrimary
                        )
                    }

                    if (totalCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${((completedCount.toFloat() / totalCount.coerceAtLeast(1)) * 100).toInt()}%",
                                style = TesseraType.TelemetryBadge,
                                color = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)
                            )
                        }
                    }
                }
            }

            // Main Flow: Either Empty, Celebration, or Active Step
            if (trackHabits.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceCard,
                    border = BorderStroke(1.dp, colors.borderHairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No Rituals in this Track",
                            style = TesseraType.SectionTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Assign habits to this sacred time (Dawn, Focus, or Twilight) in the Habit Builder.",
                            style = TesseraType.CardSubtitle,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onOpenCreateHabit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                                contentColor = HabitPalette.EmeraldMatrix.onAccent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create Ritual for this Track", style = TesseraType.TelemetryBadge)
                        }
                    }
                }
            } else if (isAllCompleted || activeItem == null) {
                // Celebration Screen
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceCard,
                    border = BorderStroke(1.dp, HabitPalette.EmeraldMatrix.level3.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(HabitPalette.EmeraldMatrix.colorForLevel(1, colors.isDark)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Text(
                            text = "Ritual Completed",
                            style = TesseraType.EditorialDateHeader,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "You showed up and cast $totalCount votes for your future identity today. Maintain the momentum.",
                            style = TesseraType.CardSubtitle,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        // Completed items list
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            trackHabits.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceElevated)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = HabitIcons.resolve(item.habit.iconKey),
                                            contentDescription = null,
                                            tint = colors.textPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = item.habit.title,
                                            style = TesseraType.CardSubtitle,
                                            color = colors.textPrimary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark),
                                contentColor = HabitPalette.EmeraldMatrix.onAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to Sanctuary", style = TesseraType.TelemetryBadge)
                        }
                    }
                }
            } else {
                // Active Step Walkthrough
                val habit = activeItem.habit
                val palette = remember(habit.colorKey) { HabitPalette.resolve(habit.colorKey) }
                val accent = palette.colorForLevel(4, colors.isDark)

                AnimatedContent(
                    targetState = activeItem,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ritualStep"
                ) { currentItem ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Current Step Header Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceCard,
                            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(palette.colorForLevel(1, colors.isDark)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = HabitIcons.resolve(currentItem.habit.iconKey),
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "STEP ${currentStepIndex + 1} OF ${pendingHabits.size}",
                                                style = TesseraType.MicroAxis,
                                                color = accent
                                            )
                                            Text(
                                                text = currentItem.habit.title,
                                                style = TesseraType.SectionTitle,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }

                                // Identity Anchor Statement (Law 4)
                                if (currentItem.habit.identityStatement.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.surfaceElevated)
                                            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "IDENTITY VOTE (LAW 4)",
                                                style = TesseraType.MicroAxis,
                                                color = accent
                                            )
                                            Text(
                                                text = "“${currentItem.habit.identityStatement}”",
                                                style = TesseraType.CardSubtitle,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }

                                // Habit Stacking Anchor Cue (Law 1)
                                if (currentItem.habit.cueAnchor.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Bolt,
                                            contentDescription = null,
                                            tint = accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "After I ${currentItem.habit.cueAnchor}",
                                            style = TesseraType.CardSubtitle,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                // 2-Minute Gateway Rule (Law 3)
                                if (currentItem.habit.microStarter.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.surfaceElevated)
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "2-MINUTE GATEWAY STARTER (LAW 3)",
                                                style = TesseraType.MicroAxis,
                                                color = colors.textMuted
                                            )
                                            Text(
                                                text = currentItem.habit.microStarter,
                                                style = TesseraType.CardSubtitle,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }

                                // Temptation Bundle Reward (Law 2)
                                if (currentItem.habit.temptationReward.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = HabitPalette.AmberWarm.colorForLevel(4, colors.isDark),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Reward: ${currentItem.habit.temptationReward}",
                                            style = TesseraType.CardSubtitle,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Interactive 2-Minute Countdown Timer
                        TwoMinuteCountdownTimer(accentColor = accent)

                        // Action Buttons: Complete Step & Advance
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCheckInHabit(currentItem.habit.id)
                                if (currentStepIndex >= pendingHabits.size - 1) {
                                    currentStepIndex = 0
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = palette.onAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "COMPLETE STEP & ADVANCE",
                                    style = TesseraType.TelemetryMedium
                                )
                            }
                        }

                        if (pendingHabits.size > 1) {
                            TextButton(
                                onClick = {
                                    currentStepIndex = (currentStepIndex + 1) % pendingHabits.size
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Skip for Now", style = TesseraType.MicroAxis, color = colors.textMuted)
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TwoMinuteCountdownTimer(
    accentColor: androidx.compose.ui.graphics.Color
) {
    val colors = TesseraTheme.colors
    val totalSeconds = 120
    var secondsRemaining by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        } else if (secondsRemaining == 0) {
            isRunning = false
        }
    }

    val minutes = secondsRemaining / 60
    val secs = secondsRemaining % 60
    val formattedTime = "%02d:%02d".format(minutes, secs)
    val progress = secondsRemaining.toFloat() / totalSeconds.toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = accentColor,
                        trackColor = colors.surfaceElevated,
                        strokeWidth = 3.5.dp
                    )
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "THE 2-MINUTE RULE GATEWAY",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                    Text(
                        text = formattedTime,
                        style = TesseraType.StreakNumber.copy(fontSize = 24.sp),
                        color = colors.textPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.borderHairline, CircleShape)
                        .clickable { isRunning = !isRunning },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.borderHairline, CircleShape)
                        .clickable {
                            isRunning = false
                            secondsRemaining = totalSeconds
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Replay,
                        contentDescription = "Reset",
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
