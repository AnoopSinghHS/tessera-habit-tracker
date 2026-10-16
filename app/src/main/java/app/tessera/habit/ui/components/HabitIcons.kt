package app.tessera.habit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class HabitIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object HabitIcons {

    val allIcons: List<HabitIconOption> = listOf(
        HabitIconOption("bolt", "Focus Bolt", Icons.Rounded.Bolt),
        HabitIconOption("terminal", "Terminal", Icons.Rounded.Terminal),
        HabitIconOption("code", "Code", Icons.Rounded.Code),
        HabitIconOption("fitness", "Strength", Icons.Rounded.FitnessCenter),
        HabitIconOption("run", "Run", Icons.Rounded.DirectionsRun),
        HabitIconOption("book", "Read", Icons.Rounded.MenuBook),
        HabitIconOption("water", "Hydrate", Icons.Rounded.WaterDrop),
        HabitIconOption("meditation", "Meditate", Icons.Rounded.SelfImprovement),
        HabitIconOption("brain", "Deep Mind", Icons.Rounded.Psychology),
        HabitIconOption("sleep", "Sleep", Icons.Rounded.Bedtime),
        HabitIconOption("flame", "Momentum", Icons.Rounded.LocalFireDepartment),
        HabitIconOption("leaf", "Nature", Icons.Rounded.Spa),
        HabitIconOption("heart", "Cardio", Icons.Rounded.Favorite),
        HabitIconOption("coffee", "No Caffeine", Icons.Rounded.LocalCafe),
        HabitIconOption("sun", "Morning", Icons.Rounded.WbSunny),
        HabitIconOption("music", "Practice", Icons.Rounded.MusicNote),
        HabitIconOption("pencil", "Journal", Icons.Rounded.Edit),
        HabitIconOption("shield", "Discipline", Icons.Rounded.Shield),
        HabitIconOption("target", "Precision", Icons.Rounded.TrackChanges),
        HabitIconOption("timer", "Pomodoro", Icons.Rounded.Timer),
        HabitIconOption("compass", "Explore", Icons.Rounded.Explore),
        HabitIconOption("bicycle", "Cycle", Icons.Rounded.DirectionsBike),
        HabitIconOption("pill", "Supplement", Icons.Rounded.Medication),
        HabitIconOption("sparkles", "Clarity", Icons.Rounded.AutoAwesome)
    )

    fun resolve(key: String?): ImageVector {
        return when (key?.trim()?.lowercase()) {
            "bolt", "zap", "focus" -> Icons.Rounded.Bolt
            "terminal", "cli" -> Icons.Rounded.Terminal
            "code", "dev" -> Icons.Rounded.Code
            "fitness", "dumbbell", "gym", "workout" -> Icons.Rounded.FitnessCenter
            "run", "running", "sprint" -> Icons.Rounded.DirectionsRun
            "book", "read", "study" -> Icons.Rounded.MenuBook
            "water", "hydration", "drop" -> Icons.Rounded.WaterDrop
            "meditation", "zen", "mindfulness" -> Icons.Rounded.SelfImprovement
            "brain", "mind", "cognition" -> Icons.Rounded.Psychology
            "sleep", "moon", "rest" -> Icons.Rounded.Bedtime
            "flame", "fire", "streak" -> Icons.Rounded.LocalFireDepartment
            "leaf", "spa", "plant", "eco" -> Icons.Rounded.Spa
            "heart", "health", "cardio" -> Icons.Rounded.Favorite
            "coffee", "cafe", "tea" -> Icons.Rounded.LocalCafe
            "sun", "morning", "light" -> Icons.Rounded.WbSunny
            "music", "audio", "instrument" -> Icons.Rounded.MusicNote
            "pencil", "pen", "journal", "write" -> Icons.Rounded.Edit
            "shield", "armor", "guard" -> Icons.Rounded.Shield
            "target", "goal", "aim" -> Icons.Rounded.TrackChanges
            "timer", "clock", "pomodoro" -> Icons.Rounded.Timer
            "compass", "navigate" -> Icons.Rounded.Explore
            "bicycle", "bike", "cycle" -> Icons.Rounded.DirectionsBike
            "pill", "vitamin", "medicine" -> Icons.Rounded.Medication
            "sparkles", "star", "magic" -> Icons.Rounded.AutoAwesome
            else -> Icons.Rounded.Bolt
        }
    }
}
