package com.example.classschedule.data.local

import com.example.classschedule.domain.model.PeriodTime
import com.example.classschedule.domain.model.Semester
import com.example.classschedule.domain.model.defaultPeriodTimes
import java.time.LocalDate
import java.time.LocalTime

fun Semester.toEntity(): SemesterEntity = SemesterEntity(
    id = id,
    name = name,
    startDateEpochDay = startDate.toEpochDay(),
    totalWeeks = totalWeeks,
    periodsPerDay = periodsPerDay,
    periodTimes = periodTimes.joinToString(";") { "${it.start}-${it.end}" }
)

fun SemesterEntity.toDomain(): Semester = Semester(
    id = id,
    name = name,
    startDate = LocalDate.ofEpochDay(startDateEpochDay),
    totalWeeks = totalWeeks,
    periodsPerDay = periodsPerDay,
    // If the semester has no stored period times, fall back to the institution's
    // standard timetable so every period shows its real clock time.
    periodTimes = parsePeriodTimes(periodTimes).ifEmpty { defaultPeriodTimes(periodsPerDay) }
)

private fun parsePeriodTimes(raw: String): List<PeriodTime> {
    if (raw.isBlank()) return emptyList()
    return raw.split(";").mapNotNull { entry ->
        val parts = entry.split("-")
        if (parts.size != 2) return@mapNotNull null
        val start = runCatching { LocalTime.parse(parts[0]) }.getOrNull()
        val end = runCatching { LocalTime.parse(parts[1]) }.getOrNull()
        if (start == null || end == null) null else PeriodTime(start, end)
    }
}
