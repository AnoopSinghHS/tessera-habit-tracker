package app.tessera.habit

import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.local.HabitLogEntity
import app.tessera.habit.data.model.BackupHabitDto
import app.tessera.habit.data.model.BackupHabitLogDto
import app.tessera.habit.data.model.HabitStats
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.data.model.TesseraBackupPayload
import app.tessera.habit.data.repository.DemoSeedGenerator
import app.tessera.habit.ui.theme.HabitPalette
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class TesseraUnitTest {

    @Test
    fun `generateSlug normalizes titles into clean URL and ADB safe slugs`() {
        assertEquals("deep-work-session", HabitEntity.generateSlug("Deep Work Session"))
        assertEquals("hydration-2-5l", HabitEntity.generateSlug("  Hydration 2.5L!  "))
        assertEquals("read-30-pages-day", HabitEntity.generateSlug("Read 30 Pages / Day"))
        assertEquals("strength-training", HabitEntity.generateSlug("---Strength___Training---"))

        val fallbackSlug = HabitEntity.generateSlug("   !!!   ")
        assertTrue(fallbackSlug.startsWith("habit-"))
    }

    @Test
    fun `calculateIntensityLevel maps binary and multi-step habits to 5 quartiles 0 to 4`() {
        // Binary habit (targetPerDay = 1)
        assertEquals(0, HabitStats.calculateIntensityLevel(count = 0, targetPerDay = 1))
        assertEquals(0, HabitStats.calculateIntensityLevel(count = -2, targetPerDay = 1))
        assertEquals(4, HabitStats.calculateIntensityLevel(count = 1, targetPerDay = 1))
        assertEquals(4, HabitStats.calculateIntensityLevel(count = 3, targetPerDay = 1))

        // Multi-step quantitative habit (targetPerDay = 4)
        assertEquals(0, HabitStats.calculateIntensityLevel(count = 0, targetPerDay = 4))
        assertEquals(1, HabitStats.calculateIntensityLevel(count = 1, targetPerDay = 4)) // 0.25 -> Level 1
        assertEquals(2, HabitStats.calculateIntensityLevel(count = 2, targetPerDay = 4)) // 0.50 -> Level 2
        assertEquals(3, HabitStats.calculateIntensityLevel(count = 3, targetPerDay = 4)) // 0.75 -> Level 3
        assertEquals(4, HabitStats.calculateIntensityLevel(count = 4, targetPerDay = 4)) // 1.00 -> Level 4
        assertEquals(4, HabitStats.calculateIntensityLevel(count = 6, targetPerDay = 4)) // >1.0 -> Level 4

        // Verify parity with HabitPalette.calculateIntensityLevel
        for (count in 0..5) {
            assertEquals(
                HabitStats.calculateIntensityLevel(count, 4),
                HabitPalette.calculateIntensityLevel(count, 4)
            )
        }
    }

    @Test
    fun `computeStats calculates current streak best streak 30d rate and weekday distribution`() {
        val today = LocalDate.of(2026, 10, 16) // Friday
        val habit = HabitEntity(
            id = 1L,
            slug = "deep-work",
            title = "Deep Work",
            targetPerDay = 2,
            weeklyGoalDays = 5
        )

        // Create a 4-day current streak ending today (daysAgo 0, 1, 2, 3)
        // Plus an older 6-day historical streak (daysAgo 10..15)
        // Plus a partial log (count = 1 < targetPerDay = 2) on daysAgo 4 (does not count as completed day for streak)
        val logs = mutableListOf<HabitLogEntity>()
        for (d in 0..3) {
            logs.add(
                HabitLogEntity(
                    habitId = 1L,
                    dateEpochDay = today.minusDays(d.toLong()).toEpochDay(),
                    count = 2
                )
            )
        }
        // Partial completion on daysAgo = 4
        logs.add(
            HabitLogEntity(
                habitId = 1L,
                dateEpochDay = today.minusDays(4L).toEpochDay(),
                count = 1
            )
        )
        // Historical 6-day streak on daysAgo 10..15
        for (d in 10..15) {
            logs.add(
                HabitLogEntity(
                    habitId = 1L,
                    dateEpochDay = today.minusDays(d.toLong()).toEpochDay(),
                    count = 2
                )
            )
        }

        val hwl = HabitWithLogs(habit = habit, logs = logs)
        val stats = hwl.computeStats(today)

        assertEquals(4, stats.currentStreak)
        assertEquals(6, stats.bestStreak)
        // Total completions = (4 * 2) + 1 + (6 * 2) = 21
        assertEquals(21, stats.totalCompletions)
        // Total active days = 4 + 1 + 6 = 11
        assertEquals(11, stats.totalActiveDays)
        // Completed days in last 30 days = 4 + 6 = 10 -> 10 / 30f
        assertEquals(10f / 30f, stats.completionRate30Days, 0.001f)
        // Weekday distribution has 7 slots (Mon..Sun) summing to totalActiveDays (11)
        assertEquals(7, stats.weekdayDistribution.size)
        assertEquals(11, stats.weekdayDistribution.sum())
    }

    @Test
    fun `computeStats preserves active streak if today is not yet logged but yesterday was completed`() {
        val today = LocalDate.of(2026, 10, 16)
        val habit = HabitEntity(
            id = 2L,
            slug = "read-30-pages",
            title = "Read 30 Pages",
            targetPerDay = 1
        )
        val logs = (1..5).map { daysAgo ->
            HabitLogEntity(
                habitId = 2L,
                dateEpochDay = today.minusDays(daysAgo.toLong()).toEpochDay(),
                count = 1
            )
        }
        val hwl = HabitWithLogs(habit = habit, logs = logs)
        val stats = hwl.computeStats(today)

        assertFalse(hwl.isCompletedOn(today))
        assertTrue(hwl.isCompletedOn(today.minusDays(1)))
        assertEquals(5, stats.currentStreak)
        assertEquals(5, stats.bestStreak)
    }

    @Test
    fun `DemoSeedGenerator generates 4 showcase habits with 20 weeks of realistic logs`() {
        val today = LocalDate.of(2026, 10, 16)
        val bundles = DemoSeedGenerator.generateShowcaseData(today)

        assertEquals(4, bundles.size)
        val slugs = bundles.map { it.habit.slug }.toSet()
        assertEquals(
            setOf("deep-work", "strength-training", "read-30-pages", "hydration"),
            slugs
        )

        for (bundle in bundles) {
            assertTrue("Expected non-empty logs for ${bundle.habit.slug}", bundle.logs.size > 50)
            val hwl = HabitWithLogs(bundle.habit, bundle.logs)
            val stats = hwl.computeStats(today)
            assertTrue("Expected positive streak for ${bundle.habit.slug}", stats.currentStreak >= 5)
            assertTrue("Expected best streak >= current streak", stats.bestStreak >= stats.currentStreak)
        }
    }

    @Test
    fun `HabitPalette resolves all 8 primary palettes and aliases with distinct 5-stop colors`() {
        assertEquals(8, HabitPalette.allPalettes.size)

        assertEquals(HabitPalette.EmeraldMatrix, HabitPalette.resolve("emerald"))
        assertEquals(HabitPalette.EmeraldMatrix, HabitPalette.resolve("LIME"))
        assertEquals(HabitPalette.CyberAmber, HabitPalette.resolve("amber"))
        assertEquals(HabitPalette.CyberAmber, HabitPalette.resolve("orange"))
        assertEquals(HabitPalette.CobaltPulse, HabitPalette.resolve("cobalt"))
        assertEquals(HabitPalette.CobaltPulse, HabitPalette.resolve("indigo"))
        assertEquals(HabitPalette.CrimsonEmber, HabitPalette.resolve("crimson"))
        assertEquals(HabitPalette.VioletNebula, HabitPalette.resolve("violet"))
        assertEquals(HabitPalette.CyanGlacier, HabitPalette.resolve("cyan"))
        assertEquals(HabitPalette.CyanGlacier, HabitPalette.resolve("glacier"))
        assertEquals(HabitPalette.RoseQuartz, HabitPalette.resolve("rose"))
        assertEquals(HabitPalette.RoseQuartz, HabitPalette.resolve("quartz"))
        assertEquals(HabitPalette.MonochromeInk, HabitPalette.resolve("monochrome"))
        assertEquals(HabitPalette.EmeraldMatrix, HabitPalette.resolve(null))
        assertEquals(HabitPalette.EmeraldMatrix, HabitPalette.resolve("unknown-key"))

        for (palette in HabitPalette.allPalettes) {
            val dark0 = palette.levelColorArgb(0, isDark = true)
            val dark1 = palette.levelColorArgb(1, isDark = true)
            val dark4 = palette.levelColorArgb(4, isDark = true)
            assertNotEquals(dark0, dark1)
            assertNotEquals(dark1, dark4)

            val light0 = palette.levelColorArgb(0, isDark = false)
            val light4 = palette.levelColorArgb(4, isDark = false)
            assertNotEquals(light0, light4)

            // Verify Compose Color stop resolution
            assertNotEquals(palette.colorForLevel(0, isDark = true), palette.colorForLevel(4, isDark = true))
            assertNotEquals(palette.colorForLevel(0, isDark = false), palette.colorForLevel(4, isDark = false))
        }
    }

    @Test
    fun `BackupModels serialize and deserialize losslessly via kotlinx serialization`() {
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val original = TesseraBackupPayload(
            version = 1,
            exportedAtEpochMs = 1760580000000L,
            appId = "app.tessera.habit",
            habits = listOf(
                BackupHabitDto(
                    slug = "deep-work",
                    title = "Deep Work Session",
                    category = "CRAFT",
                    targetPerDay = 2,
                    logs = listOf(
                        BackupHabitLogDto(
                            dateIso = "2026-10-16",
                            dateEpochDay = LocalDate.of(2026, 10, 16).toEpochDay(),
                            count = 2,
                            note = "Morning architecture block"
                        )
                    )
                )
            )
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<TesseraBackupPayload>(encoded)

        assertEquals(original.version, decoded.version)
        assertEquals(1, decoded.habits.size)
        assertEquals("deep-work", decoded.habits.first().slug)
        assertEquals(1, decoded.habits.first().logs.size)
        assertEquals("Morning architecture block", decoded.habits.first().logs.first().note)
    }

    @Test
    fun `Monday alignment for heatmap grid calculation`() {
        val date = LocalDate.of(2026, 10, 16) // Friday
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 12), monday)
    }
}
