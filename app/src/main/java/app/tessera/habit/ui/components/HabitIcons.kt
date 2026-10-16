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
 * 24 Bespoke Fine-Line Stroked Architectural Vector Icons (Lucide / Phosphor 1.75f stroke weight).
 * Zero emojis, zero heavy filled blobs — crafted to pair with Warm Limestone & Matte Carbon surfaces.
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

    // 1. "bolt" -> Geometric Octahedron / Faceted Prism (Deep Work icon from Stitch concept)
    val OctahedronPrism: ImageVector by lazy {
        fineLineIcon("OctahedronPrism") {
            strokedPath {
                // Outer hexagon contour
                moveTo(12f, 2.5f)
                lineTo(20f, 7f)
                lineTo(20f, 17f)
                lineTo(12f, 21.5f)
                lineTo(4f, 17f)
                lineTo(4f, 7f)
                close()
                // Inner equatorial triangle & facets
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
                // Inner facet ring
                moveTo(12f, 7.2f)
                lineTo(16.2f, 14.5f)
                lineTo(7.8f, 14.5f)
                close()
            }
        }
    }

    // 2. "fitness" -> Fine-Line Kettlebell (Strength Training icon from Stitch concept)
    val Kettlebell: ImageVector by lazy {
        fineLineIcon("Kettlebell") {
            strokedPath {
                // Top handle arch
                moveTo(8.5f, 8.5f)
                verticalLineTo(6.2f)
                curveTo(8.5f, 4.1f, 10.0f, 2.8f, 12.0f, 2.8f)
                curveTo(14.0f, 2.8f, 15.5f, 4.1f, 15.5f, 6.2f)
                verticalLineTo(8.5f)
                // Bell body with flat base
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

    // 3. "book" -> Fine-Line Open Book (Reading icon from Stitch concept)
    val OpenBook: ImageVector by lazy {
        fineLineIcon("OpenBook") {
            strokedPath {
                // Center spine
                moveTo(12f, 5.5f)
                verticalLineTo(19.5f)
                // Left page
                moveTo(12f, 5.5f)
                curveTo(10.2f, 4.3f, 7.5f, 4.0f, 3.5f, 4.5f)
                verticalLineTo(18.2f)
                curveTo(7.5f, 17.7f, 10.2f, 18.0f, 12f, 19.5f)
                // Right page
                moveTo(12f, 5.5f)
                curveTo(13.8f, 4.3f, 16.5f, 4.0f, 20.5f, 4.5f)
                verticalLineTo(18.2f)
                curveTo(16.5f, 17.7f, 13.8f, 18.0f, 12f, 19.5f)
            }
        }
    }

    // 4. "water" -> Fine-Line Water Droplet
    val WaterDroplet: ImageVector by lazy {
        fineLineIcon("WaterDroplet") {
            strokedPath {
                moveTo(12f, 2.8f)
                curveTo(12f, 2.8f, 5.5f, 10.2f, 5.5f, 14.8f)
                curveTo(5.5f, 18.4f, 8.4f, 21.2f, 12f, 21.2f)
                curveTo(15.6f, 21.2f, 18.5f, 18.4f, 18.5f, 14.8f)
                curveTo(18.5f, 10.2f, 12f, 2.8f, 12f, 2.8f)
                close()
                // Subtle inner highlight arc
                moveTo(9.0f, 15.2f)
                curveTo(9.0f, 16.8f, 10.2f, 18.0f, 11.8f, 18.2f)
            }
        }
    }

    // 5. "code" -> Fine-Line Code Brackets
    val CodeBrackets: ImageVector by lazy {
        fineLineIcon("CodeBrackets") {
            strokedPath {
                moveTo(16.5f, 17.5f)
                lineTo(21.5f, 12f)
                lineTo(16.5f, 6.5f)
                moveTo(7.5f, 6.5f)
                lineTo(2.5f, 12f)
                lineTo(7.5f, 17.5f)
                moveTo(14.0f, 4.5f)
                lineTo(10.0f, 19.5f)
            }
        }
    }

    // 6. "meditation" -> Fine-Line Lotus Bloom
    val LotusMeditation: ImageVector by lazy {
        fineLineIcon("LotusMeditation") {
            strokedPath {
                // Center petal
                moveTo(12f, 4.5f)
                curveTo(9.2f, 8.2f, 9.0f, 13.0f, 12f, 17.5f)
                curveTo(15.0f, 13.0f, 14.8f, 8.2f, 12f, 4.5f)
                // Left petal
                moveTo(12f, 17.5f)
                curveTo(7.5f, 16.5f, 4.5f, 12.8f, 5.2f, 8.2f)
                curveTo(7.8f, 9.0f, 9.6f, 11.2f, 10.2f, 14.0f)
                // Right petal
                moveTo(12f, 17.5f)
                curveTo(16.5f, 16.5f, 19.5f, 12.8f, 18.8f, 8.2f)
                curveTo(16.2f, 9.0f, 14.4f, 11.2f, 13.8f, 14.0f)
                // Calm horizon base
                moveTo(4.5f, 19.5f)
                horizontalLineTo(19.5f)
            }
        }
    }

    // 7. "run" -> Fine-Line Kinetic Stride / Track Wing
    val KineticRun: ImageVector by lazy {
        fineLineIcon("KineticRun") {
            strokedPath {
                // Head circle
                moveTo(16.5f, 5.0f)
                curveTo(16.5f, 6.1f, 15.6f, 7.0f, 14.5f, 7.0f)
                curveTo(13.4f, 7.0f, 12.5f, 6.1f, 12.5f, 5.0f)
                curveTo(12.5f, 3.9f, 13.4f, 3.0f, 14.5f, 3.0f)
                curveTo(15.6f, 3.0f, 16.5f, 3.9f, 16.5f, 5.0f)
                // Stride lines
                moveTo(7.0f, 9.5f)
                lineTo(11.0f, 8.0f)
                lineTo(14.5f, 10.5f)
                lineTo(18.5f, 9.0f)
                moveTo(12.5f, 9.2f)
                lineTo(9.5f, 14.5f)
                lineTo(5.0f, 16.0f)
                moveTo(10.8f, 12.5f)
                lineTo(14.8f, 15.2f)
                lineTo(16.5f, 20.5f)
            }
        }
    }

    // 8. "sleep" -> Fine-Line Crescent & Stars
    val SleepCrescent: ImageVector by lazy {
        fineLineIcon("SleepCrescent") {
            strokedPath {
                moveTo(20.2f, 14.2f)
                curveTo(18.8f, 14.8f, 17.2f, 15.0f, 15.5f, 14.6f)
                curveTo(11.6f, 13.7f, 9.0f, 9.8f, 9.8f, 5.8f)
                curveTo(10.1f, 4.6f, 10.6f, 3.6f, 11.4f, 2.8f)
                curveTo(7.2f, 3.4f, 4.0f, 7.0f, 4.0f, 11.4f)
                curveTo(4.0f, 16.2f, 7.9f, 20.2f, 12.8f, 20.2f)
                curveTo(16.4f, 20.2f, 19.4f, 17.7f, 20.2f, 14.2f)
                close()
            }
        }
    }

    // 9. "music" -> Fine-Line Eighth Note
    val MusicNote: ImageVector by lazy {
        fineLineIcon("MusicNote") {
            strokedPath {
                moveTo(9f, 18f)
                verticalLineTo(5.5f)
                lineTo(20f, 3.5f)
                verticalLineTo(16f)
                // Left note head
                moveTo(9f, 18f)
                curveTo(9f, 19.4f, 7.7f, 20.5f, 6.2f, 20.5f)
                curveTo(4.7f, 20.5f, 3.5f, 19.4f, 3.5f, 18f)
                curveTo(3.5f, 16.6f, 4.7f, 15.5f, 6.2f, 15.5f)
                curveTo(7.7f, 15.5f, 9f, 16.6f, 9f, 18f)
                // Right note head
                moveTo(20f, 16f)
                curveTo(20f, 17.4f, 18.7f, 18.5f, 17.2f, 18.5f)
                curveTo(15.7f, 18.5f, 14.5f, 17.4f, 14.5f, 16f)
                curveTo(14.5f, 14.6f, 15.7f, 13.5f, 17.2f, 13.5f)
                curveTo(18.7f, 13.5f, 20f, 14.6f, 20f, 16f)
            }
        }
    }

    // 10. "leaf" -> Fine-Line Botanical Leaf
    val BotanicalLeaf: ImageVector by lazy {
        fineLineIcon("BotanicalLeaf") {
            strokedPath {
                moveTo(11f, 20f)
                curveTo(6.5f, 20f, 4f, 16.5f, 4f, 12f)
                curveTo(4f, 6.5f, 9.5f, 3.5f, 19.5f, 3.5f)
                curveTo(19.5f, 13.5f, 16.5f, 20f, 11f, 20f)
                close()
                // Stem & central vein
                moveTo(4f, 20f)
                curveTo(7.5f, 16.5f, 11.5f, 12.5f, 15.5f, 8.5f)
            }
        }
    }

    // 11. "flame" -> Fine-Line Kindling Flame
    val KindlingFlame: ImageVector by lazy {
        fineLineIcon("KindlingFlame") {
            strokedPath {
                moveTo(12f, 3f)
                curveTo(12f, 3f, 18.5f, 8.2f, 18.5f, 13.8f)
                curveTo(18.5f, 17.6f, 15.6f, 20.8f, 12f, 20.8f)
                curveTo(8.4f, 20.8f, 5.5f, 17.6f, 5.5f, 13.8f)
                curveTo(5.5f, 10.8f, 7.3f, 8.2f, 8.8f, 6.8f)
                curveTo(8.8f, 9.2f, 10.2f, 10.2f, 10.2f, 10.2f)
                curveTo(10.2f, 7.2f, 12f, 3f, 12f, 3f)
                close()
            }
        }
    }

    // 12. "heart" -> Fine-Line Vitality Heart
    val VitalityHeart: ImageVector by lazy {
        fineLineIcon("VitalityHeart") {
            strokedPath {
                moveTo(12f, 20.2f)
                curveTo(12f, 20.2f, 3.5f, 14.8f, 3.5f, 8.8f)
                curveTo(3.5f, 5.8f, 5.8f, 3.8f, 8.5f, 3.8f)
                curveTo(10.2f, 3.8f, 11.4f, 4.7f, 12f, 6.0f)
                curveTo(12.6f, 4.7f, 13.8f, 3.8f, 15.5f, 3.8f)
                curveTo(18.2f, 3.8f, 20.5f, 5.8f, 20.5f, 8.8f)
                curveTo(20.5f, 14.8f, 12f, 20.2f, 12f, 20.2f)
                close()
            }
        }
    }

    // 13. "coffee" -> Fine-Line Ceramic Cup
    val CeramicCup: ImageVector by lazy {
        fineLineIcon("CeramicCup") {
            strokedPath {
                // Cup body
                moveTo(4f, 8f)
                horizontalLineTo(16.5f)
                verticalLineTo(14.5f)
                curveTo(16.5f, 17.5f, 14.0f, 20.0f, 10.25f, 20.0f)
                curveTo(6.5f, 20.0f, 4.0f, 17.5f, 4.0f, 14.5f)
                close()
                // Cup handle
                moveTo(16.5f, 9.5f)
                horizontalLineTo(18.2f)
                curveTo(19.8f, 9.5f, 21.0f, 10.7f, 21.0f, 12.2f)
                curveTo(21.0f, 13.7f, 19.8f, 15.0f, 18.2f, 15.0f)
                horizontalLineTo(16.5f)
                // Steam lines
                moveTo(7.5f, 3.5f)
                verticalLineTo(5.5f)
                moveTo(12.5f, 3.5f)
                verticalLineTo(5.5f)
            }
        }
    }

    // 14. "sun" -> Fine-Line Morning Sun
    val MorningSun: ImageVector by lazy {
        fineLineIcon("MorningSun") {
            strokedPath {
                // Center circle
                moveTo(16.2f, 12f)
                curveTo(16.2f, 14.3f, 14.3f, 16.2f, 12f, 16.2f)
                curveTo(9.7f, 16.2f, 7.8f, 14.3f, 7.8f, 12f)
                curveTo(7.8f, 9.7f, 9.7f, 7.8f, 12f, 7.8f)
                curveTo(14.3f, 7.8f, 16.2f, 9.7f, 16.2f, 12f)
                // Rays
                moveTo(12f, 2.5f); verticalLineTo(4.5f)
                moveTo(12f, 19.5f); verticalLineTo(21.5f)
                moveTo(2.5f, 12f); horizontalLineTo(4.5f)
                moveTo(19.5f, 12f); horizontalLineTo(21.5f)
                moveTo(5.3f, 5.3f); lineTo(6.7f, 6.7f)
                moveTo(17.3f, 17.3f); lineTo(18.7f, 18.7f)
                moveTo(18.7f, 5.3f); lineTo(17.3f, 6.7f)
                moveTo(6.7f, 17.3f); lineTo(5.3f, 18.7f)
            }
        }
    }

    // 15. "moon" -> Fine-Line Evening Moon
    val EveningMoon: ImageVector by lazy {
        fineLineIcon("EveningMoon") {
            strokedPath {
                moveTo(19.5f, 14.5f)
                curveTo(17.8f, 15.2f, 15.8f, 15.2f, 14.0f, 14.3f)
                curveTo(10.8f, 12.7f, 9.5f, 8.8f, 11.1f, 5.5f)
                curveTo(11.5f, 4.7f, 12.0f, 4.0f, 12.7f, 3.5f)
                curveTo(8.2f, 3.6f, 4.5f, 7.3f, 4.5f, 11.9f)
                curveTo(4.5f, 16.6f, 8.3f, 20.4f, 13.0f, 20.4f)
                curveTo(16.1f, 20.4f, 18.8f, 18.6f, 19.5f, 14.5f)
                close()
                // Small star cross
                moveTo(17.5f, 4.5f); verticalLineTo(7.5f)
                moveTo(16.0f, 6.0f); horizontalLineTo(19.0f)
            }
        }
    }

    // 16. "brain" -> Fine-Line Mind / Neural Hemisphere
    val MindHemisphere: ImageVector by lazy {
        fineLineIcon("MindHemisphere") {
            strokedPath {
                // Center divide
                moveTo(12f, 4.5f)
                verticalLineTo(19.5f)
                // Left hemisphere
                moveTo(12f, 5.5f)
                curveTo(10.5f, 4.0f, 7.5f, 4.2f, 6.5f, 6.5f)
                curveTo(4.5f, 7.2f, 3.8f, 9.8f, 5.0f, 11.8f)
                curveTo(3.8f, 13.8f, 4.8f, 16.5f, 7.0f, 17.2f)
                curveTo(7.8f, 19.2f, 10.5f, 19.8f, 12f, 18.2f)
                // Right hemisphere
                moveTo(12f, 5.5f)
                curveTo(13.5f, 4.0f, 16.5f, 4.2f, 17.5f, 6.5f)
                curveTo(19.5f, 7.2f, 20.2f, 9.8f, 19.0f, 11.8f)
                curveTo(20.2f, 13.8f, 19.2f, 16.5f, 17.0f, 17.2f)
                curveTo(16.2f, 19.2f, 13.5f, 19.8f, 12f, 18.2f)
            }
        }
    }

    // 17. "dumbbell" -> Fine-Line Barbell / Dumbbell
    val BarbellIcon: ImageVector by lazy {
        fineLineIcon("BarbellIcon") {
            strokedPath {
                // Center bar
                moveTo(7.5f, 12f)
                horizontalLineTo(16.5f)
                // Inner plates
                moveTo(7.5f, 7.0f); verticalLineTo(17.0f)
                moveTo(16.5f, 7.0f); verticalLineTo(17.0f)
                // Outer plates
                moveTo(4.5f, 8.8f); verticalLineTo(15.2f)
                moveTo(19.5f, 8.8f); verticalLineTo(15.2f)
                // End caps
                moveTo(2.5f, 12f); horizontalLineTo(4.5f)
                moveTo(19.5f, 12f); horizontalLineTo(21.5f)
            }
        }
    }

    // 18. "pencil" -> Fine-Line Fountain Pen / Journal Nib
    val JournalPen: ImageVector by lazy {
        fineLineIcon("JournalPen") {
            strokedPath {
                moveTo(16.5f, 3.8f)
                lineTo(20.2f, 7.5f)
                lineTo(8.0f, 19.7f)
                lineTo(3.5f, 20.5f)
                lineTo(4.3f, 16.0f)
                close()
                moveTo(13.8f, 6.5f)
                lineTo(17.5f, 10.2f)
            }
        }
    }

    // 19. "terminal" -> Fine-Line Console Prompt
    val ConsolePrompt: ImageVector by lazy {
        fineLineIcon("ConsolePrompt") {
            strokedPath {
                // Frame
                moveTo(5f, 4f)
                horizontalLineTo(19f)
                curveTo(20.1f, 4f, 21f, 4.9f, 21f, 6f)
                verticalLineTo(18f)
                curveTo(21f, 19.1f, 20.1f, 20f, 19f, 20f)
                horizontalLineTo(5f)
                curveTo(3.9f, 20f, 3f, 19.1f, 3f, 18f)
                verticalLineTo(6f)
                curveTo(3f, 4.9f, 3.9f, 4f, 5f, 4f)
                close()
                // Prompt chevron & underscore
                moveTo(7.5f, 9f)
                lineTo(10.8f, 12f)
                lineTo(7.5f, 15f)
                moveTo(12.5f, 15f)
                horizontalLineTo(16.5f)
            }
        }
    }

    // 20. "shield" -> Fine-Line Architectural Crest
    val ArchitecturalShield: ImageVector by lazy {
        fineLineIcon("ArchitecturalShield") {
            strokedPath {
                moveTo(12f, 3.0f)
                lineTo(19.5f, 6.0f)
                verticalLineTo(11.8f)
                curveTo(19.5f, 16.5f, 16.2f, 19.8f, 12f, 21.2f)
                curveTo(7.8f, 19.8f, 4.5f, 16.5f, 4.5f, 11.8f)
                verticalLineTo(6.0f)
                close()
            }
        }
    }

    // 21. "compass" -> Fine-Line Wayfinding Compass
    val WayfindingCompass: ImageVector by lazy {
        fineLineIcon("WayfindingCompass") {
            strokedPath {
                // Outer circle
                moveTo(21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
                // Diamond needle
                moveTo(16.2f, 7.8f)
                lineTo(14.0f, 14.0f)
                lineTo(7.8f, 16.2f)
                lineTo(10.0f, 10.0f)
                close()
            }
        }
    }

    // 22. "target" -> Fine-Line Concentric Target
    val ConcentricTarget: ImageVector by lazy {
        fineLineIcon("ConcentricTarget") {
            strokedPath {
                // Outer ring
                moveTo(20.5f, 12f)
                curveTo(20.5f, 16.7f, 16.7f, 20.5f, 12f, 20.5f)
                curveTo(7.3f, 20.5f, 3.5f, 16.7f, 3.5f, 12f)
                curveTo(3.5f, 7.3f, 7.3f, 3.5f, 12f, 3.5f)
                curveTo(16.7f, 3.5f, 20.5f, 7.3f, 20.5f, 12f)
                // Middle ring
                moveTo(16.2f, 12f)
                curveTo(16.2f, 14.3f, 14.3f, 16.2f, 12f, 16.2f)
                curveTo(9.7f, 16.2f, 7.8f, 14.3f, 7.8f, 12f)
                curveTo(7.8f, 9.7f, 9.7f, 7.8f, 12f, 7.8f)
                curveTo(14.3f, 7.8f, 16.2f, 9.7f, 16.2f, 12f)
                // Bullseye
                moveTo(12.8f, 12f)
                curveTo(12.8f, 12.4f, 12.4f, 12.8f, 12f, 12.8f)
                curveTo(11.6f, 12.8f, 11.2f, 12.4f, 11.2f, 12f)
                curveTo(11.2f, 11.6f, 11.6f, 11.2f, 12f, 11.2f)
                curveTo(12.4f, 11.2f, 12.8f, 11.6f, 12.8f, 12f)
            }
        }
    }

    // 23. "clock" -> Fine-Line Braun Horology Dial
    val BraunClock: ImageVector by lazy {
        fineLineIcon("BraunClock") {
            strokedPath {
                // Dial circle
                moveTo(21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(7.03f, 21f, 3f, 16.97f, 3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
                // Hands
                moveTo(12f, 7.0f)
                verticalLineTo(12f)
                lineTo(15.5f, 14.2f)
            }
        }
    }

    // 24. "sparkles" -> Fine-Line Four-Point Star
    val ClarityStar: ImageVector by lazy {
        fineLineIcon("ClarityStar") {
            strokedPath {
                moveTo(12f, 3.0f)
                curveTo(12.6f, 8.2f, 15.8f, 11.4f, 21.0f, 12.0f)
                curveTo(15.8f, 12.6f, 12.6f, 15.8f, 12.0f, 21.0f)
                curveTo(11.4f, 15.8f, 8.2f, 12.6f, 3.0f, 12.0f)
                curveTo(8.2f, 11.4f, 11.4f, 8.2f, 12.0f, 3.0f)
                close()
            }
        }
    }

    val allIcons: List<HabitIconOption> = listOf(
        HabitIconOption("bolt", "Deep Work", OctahedronPrism),
        HabitIconOption("fitness", "Kettlebell", Kettlebell),
        HabitIconOption("book", "Reading", OpenBook),
        HabitIconOption("water", "Hydration", WaterDroplet),
        HabitIconOption("code", "Code", CodeBrackets),
        HabitIconOption("meditation", "Mindfulness", LotusMeditation),
        HabitIconOption("run", "Running", KineticRun),
        HabitIconOption("sleep", "Rest", SleepCrescent),
        HabitIconOption("music", "Practice", MusicNote),
        HabitIconOption("leaf", "Botanical", BotanicalLeaf),
        HabitIconOption("flame", "Kindling", KindlingFlame),
        HabitIconOption("heart", "Vitality", VitalityHeart),
        HabitIconOption("coffee", "Ritual Cup", CeramicCup),
        HabitIconOption("sun", "Morning", MorningSun),
        HabitIconOption("moon", "Evening", EveningMoon),
        HabitIconOption("brain", "Cognition", MindHemisphere),
        HabitIconOption("dumbbell", "Strength", BarbellIcon),
        HabitIconOption("pencil", "Journal", JournalPen),
        HabitIconOption("terminal", "Console", ConsolePrompt),
        HabitIconOption("shield", "Resilience", ArchitecturalShield),
        HabitIconOption("compass", "Direction", WayfindingCompass),
        HabitIconOption("target", "Focus", ConcentricTarget),
        HabitIconOption("clock", "Cadence", BraunClock),
        HabitIconOption("sparkles", "Clarity", ClarityStar)
    )

    fun getIcon(key: String?): ImageVector = resolve(key)

    fun resolve(key: String?): ImageVector {
        return when (key?.trim()?.lowercase()) {
            "bolt", "zap", "focus", "prism", "octahedron" -> OctahedronPrism
            "fitness", "kettlebell", "gym", "workout" -> Kettlebell
            "book", "read", "reading", "study" -> OpenBook
            "water", "hydration", "drop", "droplet" -> WaterDroplet
            "code", "dev" -> CodeBrackets
            "meditation", "zen", "mindfulness", "lotus" -> LotusMeditation
            "run", "running", "sprint", "bicycle", "bike", "cycle" -> KineticRun
            "sleep", "rest", "bed" -> SleepCrescent
            "music", "audio", "instrument" -> MusicNote
            "leaf", "spa", "plant", "eco", "nature" -> BotanicalLeaf
            "flame", "fire", "streak" -> KindlingFlame
            "heart", "health", "cardio", "pill", "vitamin", "medicine" -> VitalityHeart
            "coffee", "cafe", "tea" -> CeramicCup
            "sun", "morning", "light" -> MorningSun
            "moon", "night", "evening" -> EveningMoon
            "brain", "mind", "cognition" -> MindHemisphere
            "dumbbell", "barbell", "weights" -> BarbellIcon
            "pencil", "pen", "journal", "write" -> JournalPen
            "terminal", "cli", "console" -> ConsolePrompt
            "shield", "armor", "guard" -> ArchitecturalShield
            "compass", "navigate", "explore" -> WayfindingCompass
            "target", "goal", "aim" -> ConcentricTarget
            "clock", "timer", "pomodoro", "cadence" -> BraunClock
            "sparkles", "star", "magic", "clarity" -> ClarityStar
            else -> OctahedronPrism
        }
    }
}
