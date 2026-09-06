package com.example.classschedule.domain.model

/**
 * A single course block in the timetable.
 *
 * @param days the weekdays the course meets (1 = Monday ... 7 = Sunday, ISO-8601).
 *   May contain multiple days, e.g. a twice-weekly course on Monday and Wednesday.
 * @param startPeriod 1-based index of the first period (节) the course occupies.
 * @param endPeriod 1-based inclusive index of the last period.
 * @param weekRule how the course repeats across weeks (see [WeekRule]).
 * @param weeks explicit week numbers, only used when [weekRule] == [WeekRule.RANGE].
 */
data class Course(
    val id: Long = 0,
    val name: String,
    val teacher: String = "",
    val room: String = "",
    val days: List<Int> = listOf(1),
    val startPeriod: Int,
    val endPeriod: Int,
    val weekRule: WeekRule = WeekRule.ALL,
    val weeks: List<Int> = emptyList(),
    val colorTag: ColorTag = ColorTag.BLUE,
    /** 0xFFRRGGBB when [colorTag] == [ColorTag.CUSTOM]; ignored otherwise. */
    val customColorArgb: Long? = null,
    val note: String = "",
    val semesterId: Long = 1
) {
    /** Inclusive period span: [startPeriod, endPeriod]. */
    val periodCount: Int
        get() = (endPeriod - startPeriod + 1).coerceAtLeast(1)
}
