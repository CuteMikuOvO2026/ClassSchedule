package com.example.classschedule.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a semester. [startDateEpochDay] stores [java.time.LocalDate]
 * as an epoch day (days since 1970-01-01), and [periodTimes] is a semicolon-joined
 * string of "HH:mm-HH:mm" entries, avoiding any TypeConverters.
 */
@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "本学期",
    val startDateEpochDay: Long,
    val totalWeeks: Int = 16,
    val periodsPerDay: Int = 8,
    val periodTimes: String = ""
)
