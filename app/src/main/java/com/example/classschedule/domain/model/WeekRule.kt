package com.example.classschedule.domain.model

/**
 * How a course repeats across the weeks of a semester.
 *
 * - [ALL]:  every week
 * - [ODD]:  only odd-numbered weeks (单周)
 * - [EVEN]: only even-numbered weeks (双周)
 * - [RANGE]: an explicit set/list of week numbers stored in [Course.weeks]
 */
enum class WeekRule {
    ALL,
    ODD,
    EVEN,
    RANGE
}
