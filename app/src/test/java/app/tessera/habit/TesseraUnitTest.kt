package app.tessera.habit

import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.local.HabitLogEntity
import app.tessera.habit.data.model.BackupHabitDto
import app.tessera.habit.data.model.BackupHabitLogDto
import app.tessera.habit.data.model.HabitStats
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.data.model.TesseraBackupPayload
import app.tessera.habit.ui.components.nextStreakMilestone
import app.tessera.habit.ui.components.streakDetailLabel
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.viewmodel.HabitDraft
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun `HabitDraft keeps the optional text fields when converted to a HabitEntity and back`() {
        val draft = HabitDraft(
            title = "Write 500 Words",
            category = "Craft",
            cueAnchor = "Pour morning coffee",
            temptationReward = "Drink artisanal espresso",
            microStarter = "Write 1 single sentence",
            identityStatement = "I am a disciplined writer who publishes daily",
            hasUnsavedChanges = true
        )

        val entity = draft.toHabitEntity()
        assertEquals("write-500-words", entity.slug)
        assertEquals("Write 500 Words", entity.title)
        assertEquals("Pour morning coffee", entity.cueAnchor)
        assertEquals("Drink artisanal espresso", entity.temptationReward)
        assertEquals("Write 1 single sentence", entity.microStarter)
        assertEquals("I am a disciplined writer who publishes daily", entity.identityStatement)
        assertFalse(entity.isArchived)
        assertEquals(0, entity.sortOrder)
        assertTrue(entity.createdAt > 0L)

        val hydratedDraft = HabitDraft.fromHabit(entity)
        assertEquals(draft.title, hydratedDraft.title)
        assertEquals(draft.cueAnchor, hydratedDraft.cueAnchor)
        assertEquals(draft.temptationReward, hydratedDraft.temptationReward)
        assertEquals(draft.microStarter, hydratedDraft.microStarter)
        assertEquals(draft.identityStatement, hydratedDraft.identityStatement)
        assertFalse(hydratedDraft.hasUnsavedChanges)
    }

    @Test
    fun `HabitDraft with a blank title falls back to New habit`() {
        val entity = HabitDraft(title = "   ").toHabitEntity()
        assertEquals("New habit", entity.title)
        assertEquals("new-habit", entity.slug)
    }

    @Test
    fun `editing a habit preserves isArchived sortOrder and createdAt`() {
        val stored = HabitEntity(
            id = 9L,
            slug = "vocal-riyaz",
            title = "Vocal riyaz",
            isArchived = true,
            sortOrder = 7,
            createdAt = 1700000000000L
        )

        val edited = HabitDraft.fromHabit(stored)
            .copy(title = "Vocal practice", hasUnsavedChanges = true)
            .toHabitEntity()

        assertEquals(9L, edited.id)
        assertEquals("Vocal practice", edited.title)
        assertEquals("vocal-riyaz", edited.slug)
        assertTrue(edited.isArchived)
        assertEquals(7, edited.sortOrder)
        assertEquals(1700000000000L, edited.createdAt)
        assertEquals(stored, HabitDraft.fromHabit(stored).toHabitEntity())
    }

    @Test
    fun `days tagged by the removed quick check-in still count towards streaks`() {
        val today = LocalDate.of(2026, 10, 16)
        val yesterday = today.minusDays(1)
        val legacyTag = "[GATEWAY]"
        val habit = HabitEntity(
            id = 5L,
            slug = "log-meals",
            title = "Log meals",
            targetPerDay = 3
        )
        val logs = listOf(
            HabitLogEntity(
                habitId = 5L,
                dateEpochDay = yesterday.toEpochDay(),
                count = 3,
                note = "Full day"
            ),
            HabitLogEntity(
                habitId = 5L,
                dateEpochDay = today.toEpochDay(),
                count = 1,
                note = "$legacyTag Quick version"
            )
        )
        val hwl = HabitWithLogs(habit = habit, logs = logs)

        // The tag no longer changes the heatmap level and is hidden from the note.
        assertEquals(1, hwl.intensityLevelForDate(today))
        assertEquals(4, hwl.intensityLevelForDate(yesterday))
        assertEquals("Quick version", hwl.noteForDate(today))
        assertEquals("Full day", hwl.noteForDate(yesterday))

        // It still counts as a completed day so old streaks do not break.
        val stats = hwl.computeStats(today)
        assertEquals(2, stats.currentStreak)
        assertEquals(2, stats.bestStreak)
    }

    @Test
    fun `streak milestones climb the ladder and then repeat every year`() {
        assertEquals(7, nextStreakMilestone(0))
        assertEquals(7, nextStreakMilestone(6))
        assertEquals(14, nextStreakMilestone(7))
        assertEquals(30, nextStreakMilestone(23))
        assertEquals(60, nextStreakMilestone(30))
        assertEquals(100, nextStreakMilestone(99))
        assertEquals(200, nextStreakMilestone(100))
        assertEquals(365, nextStreakMilestone(200))
        assertEquals(730, nextStreakMilestone(365))
        assertEquals(730, nextStreakMilestone(729))
        assertEquals(1095, nextStreakMilestone(730))
    }

    @Test
    fun `card status line shows milestone distance partial count or a prompt to start`() {
        assertEquals("7 days to 30", streakDetailLabel(streak = 23, todayCount = 1, targetPerDay = 1))
        assertEquals("1 day to 7", streakDetailLabel(streak = 6, todayCount = 1, targetPerDay = 1))
        assertEquals("2 of 3 today", streakDetailLabel(streak = 41, todayCount = 2, targetPerDay = 3))
        assertEquals("19 days to 60", streakDetailLabel(streak = 41, todayCount = 3, targetPerDay = 3))
        assertEquals("Start today", streakDetailLabel(streak = 0, todayCount = 0, targetPerDay = 1))
    }
}
