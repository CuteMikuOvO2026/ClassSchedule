package com.example.classschedule.data.local

import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.WeekRule

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    name = name,
    teacher = teacher,
    room = room,
    days = days.joinToString(","),
    startPeriod = startPeriod,
    endPeriod = endPeriod,
    weekRule = weekRule.name,
    weeks = weeks.joinToString(","),
    colorTag = colorTag.name,
    customColorArgb = customColorArgb,
    note = note,
    semesterId = semesterId
)

fun CourseEntity.toDomain(): Course = Course(
    id = id,
    name = name,
    teacher = teacher,
    room = room,
    days = if (days.isBlank()) listOf(1)
    else days.split(",").mapNotNull { it.trim().toIntOrNull() }.distinct().sorted(),
    startPeriod = startPeriod,
    endPeriod = endPeriod,
    weekRule = runCatching { WeekRule.valueOf(weekRule) }.getOrDefault(WeekRule.ALL),
    weeks = if (weeks.isBlank()) emptyList() else weeks.split(",").mapNotNull { it.trim().toIntOrNull() },
    colorTag = runCatching { ColorTag.valueOf(colorTag) }.getOrDefault(ColorTag.BLUE),
    customColorArgb = customColorArgb,
    note = note,
    semesterId = semesterId
)
