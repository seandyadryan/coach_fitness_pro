package com.deploydulupulangnanti.coachfitness

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TrainingTest {
    private val today = LocalDate.of(2026, 10, 19)
    @Test fun streakAllowsRestOfCurrentDayButNotAGap() {
        assertEquals(2, TrainingStats.streak(setOf(today.minusDays(1), today.minusDays(2)), today))
        assertEquals(1, TrainingStats.streak(setOf(today, today.minusDays(2)), today))
        assertEquals(0, TrainingStats.streak(setOf(today.minusDays(2)), today))
    }
    @Test fun weekStartsMondayAndExcludesFutureRecords() {
        val sessions = (-1..1).map { Session("$it", "foundation", today.plusDays(it.toLong()).toString(), 60) }
        assertEquals(1, TrainingStats.weekCount(sessions, today))
    }
    @Test fun everyRoutineHasValidMovementsAndPositiveDuration() {
        assertEquals(Catalog.exercises.size, Catalog.exercises.map { it.id }.toSet().size)
        Catalog.workouts.forEach { workout ->
            assertTrue(workout.movements().isNotEmpty())
            assertTrue(workout.minutes() > 0)
            assertTrue(workout.movements().all { it.seconds > 0 && it.cues.size >= 3 })
            assertEquals(workout.exercises.size * workout.rounds, workout.movements().size)
        }
    }
}
