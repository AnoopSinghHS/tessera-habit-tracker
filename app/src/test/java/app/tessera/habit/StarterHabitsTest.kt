package app.tessera.habit

import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.repository.StarterHabits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StarterHabitsTest {

    @Test
    fun slugsAreUniqueAndAlreadyNormalised() {
        val slugs = StarterHabits.all.map { it.slug }
        assertEquals(slugs.size, slugs.toSet().size)
        slugs.forEach { assertEquals(HabitEntity.generateSlug(it), it) }
    }

    @Test
    fun everyHabitHasATitleAndSaneTargets() {
        StarterHabits.all.forEachIndexed { index, habit ->
            assertTrue(habit.title.isNotBlank())
            assertTrue(habit.targetPerDay in 1..24)
            assertTrue(habit.weeklyGoalDays in 1..7)
            assertEquals(index, habit.sortOrder)
        }
    }
}
