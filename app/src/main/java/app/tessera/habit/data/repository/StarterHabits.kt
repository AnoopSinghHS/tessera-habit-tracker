package app.tessera.habit.data.repository

import app.tessera.habit.data.local.HabitEntity

/**
 * The owner's own habits, added once on the first launch of an empty app (see
 * [TesseraRepository.ensureStarterHabits]). Wording is a first draft meant to be edited in the app.
 *
 * `weeklyGoalDays` records the weekly target for habits that are not daily. The app does not
 * score weekly goals yet, so those habits still show a daily streak.
 */
object StarterHabits {

    val all: List<HabitEntity> = listOf(
        HabitEntity(
            slug = "up-by-630",
            colorKey = "amber",
            title = "Up and in the bathroom by 6:30",
            category = "Sleep",
            iconKey = "sun",
            description = "The tracked result of an earlier bedtime: alarm set and in bed by 11:30pm, " +
                "and an alarm app (Nuj or Alarmy) that makes me get up.",
            identityStatement = "I'm someone who starts the day early",
            cueAnchor = "Alarm set and lights out by 11:30pm; Nuj or Alarmy goes off in the morning",
            microStarter = "Dismiss the alarm and put my feet on the floor",
            sortOrder = 0
        ),
        HabitEntity(
            slug = "4k-steps",
            colorKey = "emerald",
            title = "4,000 steps",
            category = "Body",
            iconKey = "run",
            description = "Walking instead of the gym while my back heals. " +
                "Mondays: a longer cardio session.",
            identityStatement = "I'm someone who moves every day, even in a bad-back week",
            microStarter = "Walk to the end of the road and back",
            sortOrder = 1
        ),
        HabitEntity(
            slug = "practice",
            colorKey = "violet",
            title = "Practice for an hour",
            category = "Skill",
            iconKey = "stopwatch",
            description = "Four sessions a week, one hour each, while watching Netflix.",
            weeklyGoalDays = 4,
            identityStatement = "I'm someone who practises consistently",
            cueAnchor = "When I sit down to watch Netflix",
            temptationReward = "Netflix, only while I practise",
            microStarter = "Five minutes of warm-up",
            sortOrder = 2
        ),
        HabitEntity(
            slug = "study-the-gap",
            colorKey = "rose",
            title = "Study how to get better",
            category = "Skill",
            iconKey = "book",
            description = "About 1.5 hours a week: find my skill gap, then collect and work through " +
                "resources that can help me improve.",
            weeklyGoalDays = 3,
            identityStatement = "I'm someone who works on getting better on purpose",
            microStarter = "Read one page or skim one article about my gap",
            sortOrder = 3
        )
    )
}
