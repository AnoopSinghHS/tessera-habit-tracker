package app.tessera.habit.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Immutable
data class HabitIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

/**
 * 40+ Bespoke Fine-Line Stroked Architectural Vector Icons (1.75dp stroke weight, round cap/join).
 * Zero emojis, zero heavy filled shapes — tailored for Singing, Writing, Workouts, Calories, Learning & Coding.
 */
object HabitIcons {

    private fun fineLineIcon(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()
    }

    private fun ImageVector.Builder.strokedPath(
        strokeWidth: Float = 1.75f,
        pathBlock: PathBuilder.() -> Unit
    ) {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBlock
        )
    }

    // 1. "bolt" -> Geometric Octahedron / Faceted Prism (Deep Work)
    val OctahedronPrism: ImageVector by lazy {
        fineLineIcon("OctahedronPrism") {
            strokedPath {
                moveTo(12f, 2.5f)
                lineTo(20f, 7f)
                lineTo(20f, 17f)
                lineTo(12f, 21.5f)
                lineTo(4f, 17f)
                lineTo(4f, 7f)
                close()
                moveTo(12f, 2.5f)
                lineTo(12f, 12f)
                moveTo(12f, 12f)
                lineTo(20f, 7f)
                moveTo(12f, 12f)
                lineTo(4f, 7f)
                moveTo(4f, 17f)
                lineTo(12f, 12f)
                lineTo(20f, 17f)
                moveTo(12f, 12f)
                lineTo(12f, 21.5f)
                moveTo(12f, 7.2f)
                lineTo(16.2f, 14.5f)
                lineTo(7.8f, 14.5f)
                close()
            }
        }
    }

    // 2. "fitness" -> Fine-Line Kettlebell (Strength Training)
    val Kettlebell: ImageVector by lazy {
        fineLineIcon("Kettlebell") {
            strokedPath {
                moveTo(8.5f, 8.5f)
                verticalLineTo(6.2f)
                curveTo(8.5f, 4.1f, 10.0f, 2.8f, 12.0f, 2.8f)
                curveTo(14.0f, 2.8f, 15.5f, 4.1f, 15.5f, 6.2f)
                verticalLineTo(8.5f)
                moveTo(7.2f, 8.5f)
                horizontalLineTo(16.8f)
                curveTo(19.0f, 10.2f, 19.6f, 13.8f, 18.2f, 17.2f)
                curveTo(17.5f, 18.8f, 16.4f, 20.2f, 15.2f, 20.8f)
                horizontalLineTo(8.8f)
                curveTo(7.6f, 20.2f, 6.5f, 18.8f, 5.8f, 17.2f)
                curveTo(4.4f, 13.8f, 5.0f, 10.2f, 7.2f, 8.5f)
                close()
            }
        }
    }

    // 3. "book" -> Fine-Line Open Book (Reading)
    val OpenBook: ImageVector by lazy {
        fineLineIcon("OpenBook") {
            strokedPath {
                moveTo(2f, 4f)
                horizontalLineTo(8f)
                curveTo(10.2f, 4f, 12f, 5.8f, 12f, 8f)
                verticalLineTo(20f)
                curveTo(12f, 18.3f, 10.2f, 17f, 8f, 17f)
                horizontalLineTo(2f)
                verticalLineTo(4f)
                close()
                moveTo(22f, 4f)
                horizontalLineTo(16f)
                curveTo(13.8f, 4f, 12f, 5.8f, 12f, 8f)
                verticalLineTo(20f)
                curveTo(12f, 18.3f, 13.8f, 17f, 16f, 17f)
                horizontalLineTo(22f)
                verticalLineTo(4f)
                close()
            }
        }
    }

    // 4. "water" -> Fine-Line Water Droplet (Hydration)
    val WaterDroplet: ImageVector by lazy {
        fineLineIcon("WaterDroplet") {
            strokedPath {
                moveTo(12f, 2.5f)
                lineTo(7.5f, 9.2f)
                curveTo(5.6f, 12.0f, 5.6f, 15.8f, 7.5f, 18.5f)
                curveTo(9.4f, 21.2f, 12.6f, 21.8f, 15.0f, 20.2f)
                curveTo(17.4f, 18.6f, 18.5f, 15.5f, 17.5f, 12.8f)
                lineTo(12f, 2.5f)
                close()
                moveTo(9.5f, 14f)
                curveTo(9.5f, 16f, 10.8f, 17.5f, 12.5f, 17.5f)
            }
        }
    }

    // 5. "mic" / "singing" -> Vintage Studio Microphone (Riyaaz & Vocal Training)
    val StudioMic: ImageVector by lazy {
        fineLineIcon("StudioMic") {
            strokedPath {
                // Capsule body
                moveTo(9f, 6.5f)
                curveTo(9f, 4.8f, 10.3f, 3.5f, 12f, 3.5f)
                curveTo(13.7f, 3.5f, 15f, 4.8f, 15f, 6.5f)
                verticalLineTo(12f)
                curveTo(15f, 13.7f, 13.7f, 15f, 12f, 15f)
                curveTo(10.3f, 15f, 9f, 13.7f, 9f, 12f)
                close()
                // Pickup cradle arch
                moveTo(6.5f, 11f)
                verticalLineTo(12f)
                curveTo(6.5f, 15.0f, 9.0f, 17.5f, 12f, 17.5f)
                curveTo(15.0f, 17.5f, 17.5f, 15.0f, 17.5f, 12f)
                verticalLineTo(11f)
                // Stand
                moveTo(12f, 17.5f)
                verticalLineTo(21f)
                moveTo(8.5f, 21f)
                horizontalLineTo(15.5f)
            }
        }
    }

    // 6. "waveform" -> Vocal Audio Frequency Waves
    val AudioWaveform: ImageVector by lazy {
        fineLineIcon("AudioWaveform") {
            strokedPath {
                moveTo(3f, 12f)
                verticalLineTo(12f)
                moveTo(6f, 9f)
                verticalLineTo(15f)
                moveTo(9f, 6f)
                verticalLineTo(18f)
                moveTo(12f, 3f)
                verticalLineTo(21f)
                moveTo(15f, 7f)
                verticalLineTo(17f)
                moveTo(18f, 10f)
                verticalLineTo(14f)
                moveTo(21f, 12f)
                verticalLineTo(12f)
            }
        }
    }

    // 7. "clef" -> Musical Treble Clef
    val TrebleClef: ImageVector by lazy {
        fineLineIcon("TrebleClef") {
            strokedPath {
                moveTo(13f, 3f)
                curveTo(11f, 7f, 11f, 11f, 13f, 15f)
                curveTo(14.5f, 18f, 12.5f, 21f, 10.5f, 20.5f)
                curveTo(9f, 20f, 8.5f, 18.5f, 9.5f, 17f)
                curveTo(10.5f, 15.5f, 13f, 15.5f, 14f, 17f)
                moveTo(13f, 3f)
                verticalLineTo(21f)
            }
        }
    }

    // 8. "pen" / "fountain-pen" -> Fountain Pen Nib (Writing & Authoring)
    val FountainPen: ImageVector by lazy {
        fineLineIcon("FountainPen") {
            strokedPath {
                moveTo(12f, 2f)
                lineTo(15.5f, 5.5f)
                lineTo(13.5f, 14f)
                lineTo(12f, 21f)
                lineTo(10.5f, 14f)
                lineTo(8.5f, 5.5f)
                close()
                moveTo(12f, 21f)
                verticalLineTo(13f)
                moveTo(12f, 13f)
                curveTo(12.6f, 13f, 13f, 12.6f, 13f, 12f)
                curveTo(13f, 11.4f, 12.6f, 11f, 12f, 11f)
                curveTo(11.4f, 11f, 11f, 11.4f, 11f, 12f)
                curveTo(11f, 12.6f, 11.4f, 13f, 12f, 13f)
            }
        }
    }

    // 9. "pencil" -> Classic Fine-Line Pencil
    val PencilLine: ImageVector by lazy {
        fineLineIcon("PencilLine") {
            strokedPath {
                moveTo(18f, 2.5f)
                lineTo(21.5f, 6f)
                lineTo(7.5f, 20f)
                lineTo(3f, 21f)
                lineTo(4f, 16.5f)
                close()
                moveTo(15f, 5.5f)
                lineTo(18.5f, 9f)
            }
        }
    }

    // 10. "quill" -> Feather Quill
    val FeatherQuill: ImageVector by lazy {
        fineLineIcon("FeatherQuill") {
            strokedPath {
                moveTo(20.5f, 3.5f)
                curveTo(18.5f, 3.5f, 13f, 6.5f, 9.5f, 12f)
                curveTo(7.5f, 15f, 6.5f, 18.5f, 3.5f, 20.5f)
                moveTo(9.5f, 12f)
                lineTo(20.5f, 3.5f)
                moveTo(12f, 9.5f)
                lineTo(16f, 13.5f)
                moveTo(8.5f, 14f)
                lineTo(12f, 17.5f)
            }
        }
    }

    // 11. "notebook" -> Lined Journal Notebook
    val JournalNotebook: ImageVector by lazy {
        fineLineIcon("JournalNotebook") {
            strokedPath {
                moveTo(4f, 4f)
                horizontalLineTo(19f)
                curveTo(20.1f, 4f, 21f, 4.9f, 21f, 6f)
                verticalLineTo(19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                horizontalLineTo(4f)
                close()
                moveTo(7f, 4f)
                verticalLineTo(21f)
                moveTo(11f, 8f)
                horizontalLineTo(17f)
                moveTo(11f, 12f)
                horizontalLineTo(17f)
                moveTo(11f, 16f)
                horizontalLineTo(15f)
            }
        }
    }

    // 12. "laptop" -> Open Minimalist Laptop
    val LaptopWork: ImageVector by lazy {
        fineLineIcon("LaptopWork") {
            strokedPath {
                moveTo(4f, 6f)
                horizontalLineTo(20f)
                verticalLineTo(15f)
                horizontalLineTo(4f)
                close()
                moveTo(2f, 18f)
                horizontalLineTo(22f)
            }
        }
    }

    // 13. "code" -> Code Brackets
    val CodeBrackets: ImageVector by lazy {
        fineLineIcon("CodeBrackets") {
            strokedPath {
                moveTo(16f, 18f)
                lineTo(22f, 12f)
                lineTo(16f, 6f)
                moveTo(8f, 6f)
                lineTo(2f, 12f)
                lineTo(8f, 18f)
            }
        }
    }

    // 14. "terminal" -> CLI Terminal Prompt
    val TerminalPrompt: ImageVector by lazy {
        fineLineIcon("TerminalPrompt") {
            strokedPath {
                moveTo(4f, 17f)
                lineTo(10f, 12f)
                lineTo(4f, 7f)
                moveTo(12f, 17f)
                horizontalLineTo(20f)
            }
        }
    }

    // 15. "apple" -> Nutrition Apple (Calories & Diet)
    val NutritionApple: ImageVector by lazy {
        fineLineIcon("NutritionApple") {
            strokedPath {
                moveTo(12f, 6f)
                curveTo(8f, 4f, 4f, 8f, 4f, 13f)
                curveTo(4f, 18.5f, 9f, 21.5f, 12f, 21.5f)
                curveTo(15f, 21.5f, 20f, 18.5f, 20f, 13f)
                curveTo(20f, 8f, 16f, 4f, 12f, 6f)
                close()
                moveTo(12f, 6f)
                verticalLineTo(2.5f)
                moveTo(12f, 3f)
                curveTo(13.5f, 3f, 15f, 4f, 15.5f, 5f)
            }
        }
    }

    // 16. "bowl" -> Healthy Meal Bowl
    val MealBowl: ImageVector by lazy {
        fineLineIcon("MealBowl") {
            strokedPath {
                moveTo(3f, 10f)
                horizontalLineTo(21f)
                curveTo(20f, 16f, 16.5f, 19.5f, 12f, 19.5f)
                curveTo(7.5f, 19.5f, 4f, 16f, 3f, 10f)
                close()
                moveTo(8f, 19.5f)
                lineTo(8f, 21f)
                horizontalLineTo(16f)
                lineTo(16f, 19.5f)
                moveTo(8f, 7f)
                curveTo(8f, 5.5f, 9f, 4f, 9.5f, 3.5f)
                moveTo(12f, 7f)
                curveTo(12f, 5.5f, 13f, 4f, 13.5f, 3.5f)
                moveTo(16f, 7f)
                curveTo(16f, 5.5f, 17f, 4f, 17.5f, 3.5f)
            }
        }
    }

    // 17. "scale" -> Balance Weighing Scale
    val BalanceScale: ImageVector by lazy {
        fineLineIcon("BalanceScale") {
            strokedPath {
                moveTo(12f, 3f)
                verticalLineTo(21f)
                moveTo(4f, 7f)
                horizontalLineTo(20f)
                moveTo(4f, 7f)
                lineTo(2f, 13f)
                horizontalLineTo(6f)
                close()
                moveTo(20f, 7f)
                lineTo(18f, 13f)
                horizontalLineTo(22f)
                close()
                moveTo(8f, 21f)
                horizontalLineTo(16f)
            }
        }
    }

    // 18. "trending-up" -> Ascending Growth Chart with Arrow (Requested Favorite!)
    val TrendingUpChart: ImageVector by lazy {
        fineLineIcon("TrendingUpChart") {
            strokedPath {
                moveTo(22f, 7f)
                lineTo(13.5f, 15.5f)
                lineTo(8.5f, 10.5f)
                lineTo(2f, 17f)
                moveTo(16f, 7f)
                horizontalLineTo(22f)
                verticalLineTo(13f)
            }
        }
    }

    // 19. "dumbbell" -> Dumbbell Bench Weights
    val DumbbellIcon: ImageVector by lazy {
        fineLineIcon("DumbbellIcon") {
            strokedPath {
                moveTo(6.5f, 7.5f)
                lineTo(17.5f, 18.5f)
                moveTo(3f, 9.5f)
                lineTo(8.5f, 4f)
                moveTo(15.5f, 20f)
                lineTo(21f, 14.5f)
                moveTo(4.5f, 8f)
                lineTo(7f, 5.5f)
                moveTo(17f, 18.5f)
                lineTo(19.5f, 16f)
            }
        }
    }

    // 20. "barbell" -> Barbell Plate
    val BarbellPlate: ImageVector by lazy {
        fineLineIcon("BarbellPlate") {
            strokedPath {
                moveTo(2f, 12f)
                horizontalLineTo(22f)
                moveTo(6f, 6f)
                verticalLineTo(18f)
                moveTo(8.5f, 4.5f)
                verticalLineTo(19.5f)
                moveTo(15.5f, 4.5f)
                verticalLineTo(19.5f)
                moveTo(18f, 6f)
                verticalLineTo(18f)
            }
        }
    }

    // 21. "run" -> Kinetic Sprint Runner
    val KineticRun: ImageVector by lazy {
        fineLineIcon("KineticRun") {
            strokedPath {
                moveTo(14.5f, 4f)
                curveTo(15.3f, 4f, 16f, 3.3f, 16f, 2.5f)
                curveTo(16f, 1.7f, 15.3f, 1f, 14.5f, 1f)
                curveTo(13.7f, 1f, 13f, 1.7f, 13f, 2.5f)
                curveTo(13f, 3.3f, 13.7f, 4f, 14.5f, 4f)
                moveTo(7f, 21.5f)
                lineTo(10.5f, 16f)
                lineTo(9f, 11f)
                lineTo(13f, 8f)
                lineTo(16f, 10.5f)
                lineTo(19f, 9.5f)
                moveTo(9f, 11f)
                lineTo(5f, 12.5f)
                moveTo(10.5f, 16f)
                lineTo(15f, 17.5f)
                lineTo(17.5f, 21.5f)
            }
        }
    }

    // 22. "heart" -> Vitality Heart
    val VitalityHeart: ImageVector by lazy {
        fineLineIcon("VitalityHeart") {
            strokedPath {
                moveTo(19.5f, 12.5f)
                lineTo(12f, 20f)
                lineTo(4.5f, 12.5f)
                curveTo(2.5f, 10.5f, 2.5f, 7.2f, 4.5f, 5.2f)
                curveTo(6.5f, 3.2f, 9.8f, 3.2f, 11.8f, 5.2f)
                lineTo(12f, 5.4f)
                lineTo(12.2f, 5.2f)
                curveTo(14.2f, 3.2f, 17.5f, 3.2f, 19.5f, 5.2f)
                curveTo(21.5f, 7.2f, 21.5f, 10.5f, 19.5f, 12.5f)
                close()
            }
        }
    }

    // 23. "flame" -> Kindling Flame (Metabolism / Streak)
    val KindlingFlame: ImageVector by lazy {
        fineLineIcon("KindlingFlame") {
            strokedPath {
                moveTo(8.5f, 14.5f)
                curveTo(8.5f, 17f, 10f, 19.5f, 12f, 19.5f)
                curveTo(14f, 19.5f, 15.5f, 17f, 15.5f, 14.5f)
                curveTo(15.5f, 11f, 12f, 9.5f, 12f, 9.5f)
                curveTo(12f, 9.5f, 8.5f, 11f, 8.5f, 14.5f)
                close()
                moveTo(12f, 3.5f)
                curveTo(12f, 3.5f, 6f, 9f, 6f, 14.5f)
                curveTo(6f, 18.5f, 8.8f, 21.5f, 12f, 21.5f)
                curveTo(15.2f, 21.5f, 18f, 18.5f, 18f, 14.5f)
                curveTo(18f, 9f, 12f, 3.5f, 12f, 3.5f)
                close()
            }
        }
    }

    // 24. "clock" -> Braun Analog Clock
    val BraunClock: ImageVector by lazy {
        fineLineIcon("BraunClock") {
            strokedPath {
                moveTo(21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
                moveTo(12f, 7.0f)
                verticalLineTo(12f)
                lineTo(15.5f, 14.2f)
            }
        }
    }

    // 25. "stopwatch" -> Precision Stopwatch
    val StopwatchIcon: ImageVector by lazy {
        fineLineIcon("StopwatchIcon") {
            strokedPath {
                moveTo(12f, 9f)
                verticalLineTo(14f)
                moveTo(12f, 5f)
                verticalLineTo(2f)
                moveTo(10f, 2f)
                horizontalLineTo(14f)
                moveTo(21f, 14f)
                curveTo(21f, 18.97f, 16.97f, 23f, 12f, 23f)
                curveTo(7.03f, 23f, 3f, 18.97f, 3f, 14f)
                curveTo(3f, 9.03f, 7.03f, 5f, 12f, 5f)
                curveTo(16.97f, 5f, 21f, 9.03f, 21f, 14f)
            }
        }
    }

    // 26. "hourglass" -> Hourglass
    val HourglassIcon: ImageVector by lazy {
        fineLineIcon("HourglassIcon") {
            strokedPath {
                moveTo(5f, 3f)
                horizontalLineTo(19f)
                moveTo(5f, 21f)
                horizontalLineTo(19f)
                moveTo(6f, 3f)
                lineTo(12f, 12f)
                lineTo(6f, 21f)
                moveTo(18f, 3f)
                lineTo(12f, 12f)
                lineTo(18f, 21f)
            }
        }
    }

    // 27. "books" -> Stacked Books
    val StackedBooks: ImageVector by lazy {
        fineLineIcon("StackedBooks") {
            strokedPath {
                moveTo(4f, 19f)
                horizontalLineTo(20f)
                moveTo(4f, 15f)
                horizontalLineTo(20f)
                moveTo(4f, 11f)
                horizontalLineTo(20f)
                moveTo(6f, 19f)
                verticalLineTo(7f)
                curveTo(6f, 5.5f, 7.5f, 4f, 9f, 4f)
                horizontalLineTo(18f)
                verticalLineTo(19f)
            }
        }
    }

    // 28. "bookmark" -> Ribbon Bookmark
    val BookmarkRibbon: ImageVector by lazy {
        fineLineIcon("BookmarkRibbon") {
            strokedPath {
                moveTo(6f, 3f)
                horizontalLineTo(18f)
                verticalLineTo(21f)
                lineTo(12f, 16.5f)
                lineTo(6f, 21f)
                close()
            }
        }
    }

    // 29. "sprout" -> Sprout of Growth
    val GrowthSprout: ImageVector by lazy {
        fineLineIcon("GrowthSprout") {
            strokedPath {
                moveTo(12f, 22f)
                verticalLineTo(12f)
                moveTo(12f, 12f)
                curveTo(12f, 7f, 8f, 5f, 4f, 5f)
                curveTo(4f, 9f, 7f, 12f, 12f, 12f)
                moveTo(12f, 15f)
                curveTo(14f, 11f, 17f, 9f, 20f, 9f)
                curveTo(20f, 12f, 18f, 15f, 12f, 15f)
            }
        }
    }

    // 30. "target" -> Concentric Target Bullseye
    val ConcentricTarget: ImageVector by lazy {
        fineLineIcon("ConcentricTarget") {
            strokedPath {
                moveTo(21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
                moveTo(16.5f, 12f)
                curveTo(16.5f, 14.5f, 14.5f, 16.5f, 12f, 16.5f)
                curveTo(9.5f, 16.5f, 7.5f, 14.5f, 7.5f, 12f)
                curveTo(7.5f, 9.5f, 9.5f, 7.5f, 12f, 7.5f)
                curveTo(14.5f, 7.5f, 16.5f, 9.5f, 16.5f, 12f)
            }
        }
    }

    // 31. "compass" -> Wayfinding Compass
    val WayfindingCompass: ImageVector by lazy {
        fineLineIcon("WayfindingCompass") {
            strokedPath {
                moveTo(21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
                moveTo(15.5f, 8.5f)
                lineTo(13.5f, 13.5f)
                lineTo(8.5f, 15.5f)
                lineTo(10.5f, 10.5f)
                close()
            }
        }
    }

    // 32. "star" -> North Star
    val NorthStar: ImageVector by lazy {
        fineLineIcon("NorthStar") {
            strokedPath {
                moveTo(12f, 2f)
                lineTo(14.5f, 9.5f)
                lineTo(22f, 12f)
                lineTo(14.5f, 14.5f)
                lineTo(12f, 22f)
                lineTo(9.5f, 14.5f)
                lineTo(2f, 12f)
                lineTo(9.5f, 9.5f)
                close()
            }
        }
    }

    val allIcons: List<HabitIconOption> = listOf(
        // Singing & Music
        HabitIconOption("mic", "Studio Mic", StudioMic),
        HabitIconOption("waveform", "Vocal Wave", AudioWaveform),
        HabitIconOption("clef", "Treble Clef", TrebleClef),
        HabitIconOption("bolt", "Deep Work", OctahedronPrism),
        // Writing & Craft
        HabitIconOption("pen", "Fountain Pen", FountainPen),
        HabitIconOption("pencil", "Pencil", PencilLine),
        HabitIconOption("quill", "Feather Quill", FeatherQuill),
        HabitIconOption("notebook", "Journal", JournalNotebook),
        // Reading & Learning
        HabitIconOption("book", "Open Book", OpenBook),
        HabitIconOption("books", "Stacked Books", StackedBooks),
        HabitIconOption("bookmark", "Bookmark", BookmarkRibbon),
        // Fitness & Physical
        HabitIconOption("fitness", "Kettlebell", Kettlebell),
        HabitIconOption("dumbbell", "Dumbbell", DumbbellIcon),
        HabitIconOption("barbell", "Barbell", BarbellPlate),
        HabitIconOption("run", "Running", KineticRun),
        HabitIconOption("heart", "Vitality", VitalityHeart),
        // Calories & Nutrition
        HabitIconOption("apple", "Nutrition", NutritionApple),
        HabitIconOption("water", "Hydration", WaterDroplet),
        HabitIconOption("bowl", "Meal", MealBowl),
        HabitIconOption("scale", "Scale", BalanceScale),
        HabitIconOption("flame", "Metabolism", KindlingFlame),
        // Tech & Coding
        HabitIconOption("laptop", "Laptop", LaptopWork),
        HabitIconOption("code", "Code", CodeBrackets),
        HabitIconOption("terminal", "Terminal", TerminalPrompt),
        // Time & Cadence
        HabitIconOption("clock", "Horology", BraunClock),
        HabitIconOption("stopwatch", "Stopwatch", StopwatchIcon),
        HabitIconOption("hourglass", "Hourglass", HourglassIcon),
        // Growth & Direction
        HabitIconOption("trending-up", "Trending Up", TrendingUpChart),
        HabitIconOption("sprout", "Growth", GrowthSprout),
        HabitIconOption("target", "Target", ConcentricTarget),
        HabitIconOption("compass", "Compass", WayfindingCompass),
        HabitIconOption("star", "North Star", NorthStar)
    )

    fun getIcon(key: String?): ImageVector = resolve(key)

    fun resolve(key: String?): ImageVector {
        return when (key?.trim()?.lowercase()) {
            "mic", "microphone", "sing", "singing", "vocal", "riyaaz", "voice" -> StudioMic
            "waveform", "audio", "sound", "freq" -> AudioWaveform
            "clef", "treble", "music", "song" -> TrebleClef
            "pen", "fountain-pen", "nib" -> FountainPen
            "pencil", "write", "writing" -> PencilLine
            "quill", "feather", "author" -> FeatherQuill
            "notebook", "journal", "diary" -> JournalNotebook
            "book", "read", "reading", "study" -> OpenBook
            "books", "stacked-books", "library" -> StackedBooks
            "bookmark", "ribbon" -> BookmarkRibbon
            "fitness", "kettlebell", "gym" -> Kettlebell
            "dumbbell", "lift", "weights" -> DumbbellIcon
            "barbell", "bench", "press" -> BarbellPlate
            "run", "running", "sprint", "cardio" -> KineticRun
            "heart", "vitality", "pulse" -> VitalityHeart
            "apple", "calories", "diet", "nutrition", "food" -> NutritionApple
            "water", "hydration", "drop", "droplet" -> WaterDroplet
            "bowl", "meal", "eating" -> MealBowl
            "scale", "weigh", "weight" -> BalanceScale
            "flame", "burn", "streak", "metabolism" -> KindlingFlame
            "laptop", "pc", "computer", "desk" -> LaptopWork
            "code", "coding", "dev", "program" -> CodeBrackets
            "terminal", "cli", "console", "bash" -> TerminalPrompt
            "clock", "time", "pomodoro", "cadence" -> BraunClock
            "stopwatch", "timer" -> StopwatchIcon
            "hourglass", "sand" -> HourglassIcon
            "trending-up", "trending", "growth", "chart", "stonks", "up" -> TrendingUpChart
            "sprout", "plant", "habit", "grow" -> GrowthSprout
            "target", "goal", "aim", "bullseye" -> ConcentricTarget
            "compass", "direction", "explore" -> WayfindingCompass
            "star", "clarity", "north-star" -> NorthStar
            "bolt", "zap", "focus", "prism", "octahedron" -> OctahedronPrism
            else -> OctahedronPrism
        }
    }
}
