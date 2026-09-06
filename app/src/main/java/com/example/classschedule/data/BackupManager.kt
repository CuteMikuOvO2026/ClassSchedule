package com.example.classschedule.data

import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.Semester
import com.example.classschedule.domain.model.WeekRule
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Export/import the timetable as JSON using Android's built-in [org.json] —
 * no extra serialization dependency required. Used by the Settings backup UI.
 */
class BackupManager(
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository
) {

    suspend fun exportJson(): String {
        val semester = semesterRepository.getFirst()
        val courses = courseRepository.observeCourses(semester?.id ?: 1).first()

        val sem = JSONObject().apply {
            put("id", semester?.id ?: 1L)
            put("name", semester?.name ?: "本学期")
            put("startDateEpochDay", semester?.startDate?.toEpochDay() ?: LocalDate.now().toEpochDay())
            put("totalWeeks", semester?.totalWeeks ?: 16)
            put("periodsPerDay", semester?.periodsPerDay ?: 8)
        }
        val courseArray = JSONArray()
        courses.forEach { c ->
            courseArray.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("teacher", c.teacher)
                    put("room", c.room)
                    put("days", JSONArray(c.days))
                    put("startPeriod", c.startPeriod)
                    put("endPeriod", c.endPeriod)
                    put("weekRule", c.weekRule.name)
                    put("weeks", JSONArray(c.weeks))
                    put("colorTag", c.colorTag.name)
                    c.customColorArgb?.let { put("customColorArgb", it) }
                    put("note", c.note)
                    put("semesterId", c.semesterId)
                }
            )
        }

        return JSONObject().apply {
            put("version", 1)
            put("semester", sem)
            put("courses", courseArray)
        }.toString(2)
    }

    suspend fun importFromJson(json: String): Boolean = try {
        val root = JSONObject(json)
        val sem = root.getJSONObject("semester")
        val defaultStart = LocalDate.now().toEpochDay()
        semesterRepository.upsert(
            Semester(
                id = sem.optLong("id", 1),
                name = sem.optString("name", "本学期"),
                startDate = LocalDate.ofEpochDay(sem.optLong("startDateEpochDay", defaultStart)),
                totalWeeks = sem.optInt("totalWeeks", 16),
                periodsPerDay = sem.optInt("periodsPerDay", 8)
            )
        )
        val courses = root.optJSONArray("courses") ?: JSONArray()
        for (i in 0 until courses.length()) {
            val o = courses.getJSONObject(i)
            val weeksArray = o.optJSONArray("weeks")
            val weeks = if (weeksArray != null) {
                (0 until weeksArray.length()).map { weeksArray.getInt(it) }
            } else emptyList()
            val daysArray = o.optJSONArray("days")
            val days = if (daysArray != null) {
                (0 until daysArray.length()).map { daysArray.getInt(it) }
            } else listOf(o.optInt("dayOfWeek", 1))
            courseRepository.upsert(
                Course(
                    id = o.optLong("id", 0),
                    name = o.getString("name"),
                    teacher = o.optString("teacher", ""),
                    room = o.optString("room", ""),
                    days = days,
                    startPeriod = o.optInt("startPeriod", 1),
                    endPeriod = o.optInt("endPeriod", 1),
                    weekRule = runCatching { WeekRule.valueOf(o.optString("weekRule", "")) }.getOrDefault(WeekRule.ALL),
                    weeks = weeks,
                    colorTag = runCatching { ColorTag.valueOf(o.optString("colorTag", "")) }.getOrDefault(ColorTag.BLUE),
                    customColorArgb = o.optLong("customColorArgb", 0).takeIf { it != 0L },
                    note = o.optString("note", ""),
                    semesterId = o.optLong("semesterId", sem.optLong("id", 1))
                )
            )
        }
        true
    } catch (e: Exception) {
        false
    }
}
