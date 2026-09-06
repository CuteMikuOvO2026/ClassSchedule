package com.example.classschedule.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * A period (节) boundary — when a class starts and finishes.
 */
data class PeriodTime(
    val start: LocalTime,
    val end: LocalTime
)

/**
 * A semester defines the calendar context for the timetable.
 *
 * @param startDate the date of week 1, Monday (the first teaching day).
 * @param totalWeeks number of teaching weeks (default 16 for a typical semester).
 * @param periodsPerDay number of periods per day (default 8).
 * @param periodTimes schedule per period; when empty the UI falls back to [defaultPeriodTimes].
 */
data class Semester(
    val id: Long = 1,
    val name: String = "本学期",
    val startDate: LocalDate,
    val totalWeeks: Int = 16,
    val periodsPerDay: Int = 8,
    val periodTimes: List<PeriodTime> = emptyList()
)

/**
 * The institution's real class timetable (morning / afternoon / evening).
 * Period 1-4 morning, 5-8 afternoon, 9-12 evening.
 */
val DEFAULT_PERIOD_TIMES: List<PeriodTime> = listOf(
    PeriodTime(LocalTime.of(8, 20), LocalTime.of(9, 5)),
    PeriodTime(LocalTime.of(9, 15), LocalTime.of(10, 0)),
    PeriodTime(LocalTime.of(10, 20), LocalTime.of(11, 5)),
    PeriodTime(LocalTime.of(11, 15), LocalTime.of(12, 0)),
    PeriodTime(LocalTime.of(14, 0), LocalTime.of(14, 45)),
    PeriodTime(LocalTime.of(14, 55), LocalTime.of(15, 40)),
    PeriodTime(LocalTime.of(16, 0), LocalTime.of(16, 45)),
    PeriodTime(LocalTime.of(16, 55), LocalTime.of(17, 40)),
    PeriodTime(LocalTime.of(18, 10), LocalTime.of(18, 55)),
    PeriodTime(LocalTime.of(19, 5), LocalTime.of(19, 50)),
    PeriodTime(LocalTime.of(20, 0), LocalTime.of(20, 45)),
    PeriodTime(LocalTime.of(20, 55), LocalTime.of(21, 40))
)

/**
 * Returns the first [periodsPerDay] entries of the default class timetable.
 * Used whenever a semester has no custom period times.
 */
fun defaultPeriodTimes(periodsPerDay: Int = 8): List<PeriodTime> =
    DEFAULT_PERIOD_TIMES.take(periodsPerDay.coerceIn(0, DEFAULT_PERIOD_TIMES.size))
