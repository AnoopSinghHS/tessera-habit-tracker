package app.tessera.habit.data.repository

import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.local.HabitLogEntity
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

object DemoSeedGenerator {

    data class SeedHabitBundle(
        val habit: HabitEntity,
        val logs: List<HabitLogEntity>
    )

    fun generateShowcaseData(today: LocalDate = LocalDate.now()): List<SeedHabitBundle> {
        val nowMs = System.currentTimeMillis()
        val totalDays = 140 // 20 weeks of realistic GitHub heatmap history

        val habits = listOf(
            HabitEntity(
                slug = "deep-work",
                title = "Deep Work Session",
                description = "90m uninterrupted architecture & coding blocks",
                category = "CRAFT",
                iconKey = "bolt",
                colorKey = "emerald",
                unitLabel = "blocks",
                targetPerDay = 2,
                weeklyGoalDays = 5,
                sortOrder = 0,
                createdAt = nowMs - 140L * 86_400_000L
            ),
            HabitEntity(
                slug = "strength-training",
                title = "Strength Training",
                description = "Compound lifts & progressive overload session",
                category = "BODY",
                iconKey = "fitness",
                colorKey = "cyan",
                unitLabel = "sessions",
                targetPerDay = 1,
                weeklyGoalDays = 4,
                sortOrder = 1,
                createdAt = nowMs - 140L * 86_400_000L
            ),
            HabitEntity(
                slug = "read-30-pages",
                title = "Read 30 Pages",
                description = "Non-fiction, systems engineering, or philosophy before bed",
                category = "MIND",
                iconKey = "book",
                colorKey = "violet",
                unitLabel = "pages",
                targetPerDay = 1,
                weeklyGoalDays = 7,
                sortOrder = 2,
                createdAt = nowMs - 140L * 86_400_000L
            ),
            HabitEntity(
                slug = "hydration",
                title = "Hydration 2.5L",
                description = "Electrolytes + 4x 600ml insulated flasks throughout day",
                category = "HEALTH",
                iconKey = "water",
                colorKey = "amber",
                unitLabel = "flasks",
                targetPerDay = 4,
                weeklyGoalDays = 7,
                sortOrder = 3,
                createdAt = nowMs - 140L * 86_400_000L
            )
        )

        return habits.mapIndexed { index, habit ->
            val rng = Random(20261016L + index * 997L)
            val logs = mutableListOf<HabitLogEntity>()

            for (daysAgo in totalDays downTo 0) {
                val date = today.minusDays(daysAgo.toLong())
                val epochDay = date.toEpochDay()
                val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

                val count: Int = when (habit.slug) {
                    "deep-work" -> {
                        // Strong recent 12-day streak, realistic weekday focus
                        if (daysAgo in 0..11) {
                            2
                        } else if (isWeekend) {
                            if (rng.nextFloat() < 0.35f) 1 else 0
                        } else {
                            val roll = rng.nextFloat()
                            when {
                                roll < 0.65f -> 2
                                roll < 0.85f -> 1
                                else -> 0
                            }
                        }
                    }

                    "strength-training" -> {
                        // Mon / Tue / Thu / Sat cadence with high consistency
                        val isLiftDay = date.dayOfWeek in setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.SATURDAY
                        )
                        if (daysAgo in 0..5) {
                            1
                        } else if (isLiftDay && rng.nextFloat() < 0.84f) {
                            1
                        } else if (!isLiftDay && rng.nextFloat() < 0.15f) {
                            1
                        } else {
                            0
                        }
                    }

                    "read-30-pages" -> {
                        // High daily consistency with a 19-day active streak
                        if (daysAgo in 0..18) {
                            1
                        } else if (rng.nextFloat() < 0.80f) {
                            1
                        } else {
                            0
                        }
                    }

                    "hydration" -> {
                        // Multi-step 0..4 intensity shading showcase
                        if (daysAgo == 0) {
                            3 // 3/4 today so user can tap +1 on dashboard or widget to complete 4/4!
                        } else if (daysAgo in 1..9) {
                            4
                        } else {
                            val roll = rng.nextFloat()
                            when {
                                roll < 0.52f -> 4
                                roll < 0.75f -> 3
                                roll < 0.90f -> 2
                                roll < 0.96f -> 1
                                else -> 0
                            }
                        }
                    }

                    else -> 0
                }

                if (count > 0) {
                    val sampleNote = when {
                        daysAgo == 0 && habit.slug == "deep-work" -> "Completed morning architecture sprint"
                        daysAgo == 1 && habit.slug == "strength-training" -> "Heavy squat & overhead press"
                        daysAgo == 2 && habit.slug == "read-30-pages" -> "Finished chapter on distributed systems"
                        else -> ""
                    }
                    logs.add(
                        HabitLogEntity(
                            habitId = 0, // Populated after inserting parent HabitEntity
                            dateEpochDay = epochDay,
                            count = count,
                            note = sampleNote,
                            updatedAt = nowMs - daysAgo * 86_400_000L
                        )
                    )
                }
            }

            SeedHabitBundle(habit = habit, logs = logs)
        }
    }
}
