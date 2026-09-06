package com.example.classschedule.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.WeekRule

/**
 * Room entity for a course. Enum values are stored as their [Enum.name] strings
 * and the [Course.weeks] list is stored as a comma-joined [String] so no
 * TypeConverters are required.
 */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val teacher: String = "",
    val room: String = "",
    val days: String = "1",
    val startPeriod: Int,
    val endPeriod: Int,
    val weekRule: String = WeekRule.ALL.name,
    val weeks: String = "",
    val colorTag: String = ColorTag.BLUE.name,
    val customColorArgb: Long? = null,
    val note: String = "",
    val semesterId: Long = 1
)
