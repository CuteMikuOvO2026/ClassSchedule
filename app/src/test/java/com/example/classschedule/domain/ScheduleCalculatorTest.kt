package com.example.classschedule.domain

import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.WeekRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleCalculatorTest {

    // Semester start should be a Monday in real use; weekOfDate only uses day counts.
    private val semesterStart = LocalDate.of(2026, 9, 7)

    private fun course(
        day: Int,
        start: Int,
        end: Int,
        rule: WeekRule = WeekRule.ALL,
        weeks: List<Int> = emptyList()
    ) = Course(
        name = "C",
        days = listOf(day),
        startPeriod = start,
        endPeriod = end,
        weekRule = rule,
        weeks = weeks,
        colorTag = ColorTag.BLUE
    )

    @Test
    fun weekOfDate_returnsZeroBeforeStart() {
        assertEquals(0, ScheduleCalculator.weekOfDate(semesterStart.minusDays(1), semesterStart))
    }

    @Test
    fun weekOfDate_weekOneThroughSevenDays() {
        assertEquals(1, ScheduleCalculator.weekOfDate(semesterStart, semesterStart))
        assertEquals(1, ScheduleCalculator.weekOfDate(semesterStart.plusDays(6), semesterStart))
        assertEquals(2, ScheduleCalculator.weekOfDate(semesterStart.plusDays(7), semesterStart))
        assertEquals(3, ScheduleCalculator.weekOfDate(semesterStart.plusDays(14), semesterStart))
    }

    @Test
    fun isCourseInWeek_allRule_alwaysShown() {
        val c = course(1, 1, 2)
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 1))
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 10))
    }

    @Test
    fun isCourseInWeek_oddRule() {
        val c = course(1, 1, 2, rule = WeekRule.ODD)
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 1))
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 3))
        assertFalse(ScheduleCalculator.isCourseInWeek(c, 2))
        assertFalse(ScheduleCalculator.isCourseInWeek(c, 4))
    }

    @Test
    fun isCourseInWeek_evenRule() {
        val c = course(1, 1, 2, rule = WeekRule.EVEN)
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 2))
        assertFalse(ScheduleCalculator.isCourseInWeek(c, 1))
    }

    @Test
    fun isCourseInWeek_rangeRule() {
        val c = course(1, 1, 2, rule = WeekRule.RANGE, weeks = listOf(2, 5))
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 2))
        assertTrue(ScheduleCalculator.isCourseInWeek(c, 5))
        assertFalse(ScheduleCalculator.isCourseInWeek(c, 3))
    }

    @Test
    fun dateForWeekAndDay_isConsistent() {
        // Week 1, Monday == start; Sunday == start + 6
        assertEquals(semesterStart, ScheduleCalculator.dateForWeekAndDay(semesterStart, 1, 1))
        assertEquals(semesterStart.plusDays(6), ScheduleCalculator.dateForWeekAndDay(semesterStart, 1, 7))
        // Week 2, Monday == start + 7
        assertEquals(semesterStart.plusDays(7), ScheduleCalculator.dateForWeekAndDay(semesterStart, 2, 1))
    }

    @Test
    fun weekDateRange_coversMondayToSunday() {
        val (monday, sunday) = ScheduleCalculator.weekDateRange(semesterStart, 2)
        assertEquals(semesterStart.plusDays(7), monday)
        assertEquals(semesterStart.plusDays(13), sunday)
    }

    @Test
    fun periodCount_isInclusiveSpan() {
        assertEquals(2, course(1, 1, 2).periodCount)
        assertEquals(1, course(1, 3, 3).periodCount)
    }
}
