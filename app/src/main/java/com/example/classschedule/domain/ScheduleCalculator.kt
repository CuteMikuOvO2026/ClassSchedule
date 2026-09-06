package com.example.classschedule.domain

import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.WeekRule
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure, side-effect-free date / week rules for the timetable.
 * Kept free of Android and Compose dependencies so it is trivially unit-testable.
 */
object ScheduleCalculator {

    /**
     * Which 1-based teaching week [date] falls in, relative to a semester that
     * starts on [semesterStart] (a Monday).
     *
     * @return the week number (>= 1), or 0 if [date] is before the semester starts.
     */
    fun weekOfDate(date: LocalDate, semesterStart: LocalDate): Int {
        if (date.isBefore(semesterStart)) return 0
        val daysBetween = ChronoUnit.DAYS.between(semesterStart, date)
        return (daysBetween / 7).toInt() + 1
    }

    /**
     * Whether [course] is shown in teaching week [week] (1-based), honouring its
     * [Course.weekRule].
     */
    fun isCourseInWeek(course: Course, week: Int): Boolean = when (course.weekRule) {
        WeekRule.ALL -> true
        WeekRule.ODD -> week % 2 == 1
        WeekRule.EVEN -> week % 2 == 0
        WeekRule.RANGE -> week in course.weeks
    }

    /**
     * The calendar date for a given week + ISO day-of-week (1 = Monday ... 7 = Sunday),
     * assuming [semesterStart] is the Monday of week 1.
     */
    fun dateForWeekAndDay(semesterStart: LocalDate, week: Int, dayOfWeek: Int): LocalDate =
        semesterStart.plusWeeks((week - 1).toLong()).plusDays((dayOfWeek - 1).toLong())

    /** Monday..Sunday range for [week]. */
    fun weekDateRange(semesterStart: LocalDate, week: Int): Pair<LocalDate, LocalDate> {
        val monday = dateForWeekAndDay(semesterStart, week, 1)
        return monday to monday.plusDays(6)
    }

    /**
     * Parses a comma-separated week string such as "1,3,5" or "1-8" (or a mix)
     * into a distinct, ascending list of weeks. Empty/invalid tokens are ignored.
     */
    fun parseWeekList(text: String): List<Int> {
        val set = linkedSetOf<Int>()
        text.split(",").forEach { part ->
            val trimmed = part.trim()
            if (trimmed.isBlank()) return@forEach
            val range = trimmed.split("-")
            if (range.size == 2) {
                val a = range[0].trim().toIntOrNull()
                val b = range[1].trim().toIntOrNull()
                if (a != null && b != null && a <= b) {
                    for (w in a..b) if (w >= 1) set += w
                }
            } else {
                trimmed.toIntOrNull()?.let { if (it >= 1) set += it }
            }
        }
        return set.toList()
    }

    /**
     * Returns courses that overlap on the same day and period span.
     * Useful for surfacing scheduling conflicts. (Reserved for a later milestone.)
     */
    fun findOverlaps(courses: List<Course>): List<Pair<Course, Course>> {
        val overlaps = mutableListOf<Pair<Course, Course>>()
        // A course may meet on several days; compare courses that share a weekday.
        for (day in 1..7) {
            val dayCourses = courses.filter { day in it.days }.sortedBy { it.startPeriod }
            for (i in dayCourses.indices) {
                for (j in i + 1 until dayCourses.size) {
                    val a = dayCourses[i]
                    val b = dayCourses[j]
                    if (b.startPeriod <= a.endPeriod) {
                        overlaps += a to b
                    } else {
                        break // sorted by start; no later course can overlap
                    }
                }
            }
        }
        return overlaps.distinct()
    }
}
