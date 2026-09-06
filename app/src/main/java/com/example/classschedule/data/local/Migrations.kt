package com.example.classschedule.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: the single `dayOfWeek` (INTEGER) column was replaced by a multi-day
 * `days` (TEXT) column. Rebuild the table so this works on every API level
 * (SQLite DROP COLUMN needs 3.35+, not available on older devices).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS courses_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                teacher TEXT NOT NULL,
                room TEXT NOT NULL,
                days TEXT NOT NULL,
                startPeriod INTEGER NOT NULL,
                endPeriod INTEGER NOT NULL,
                weekRule TEXT NOT NULL,
                weeks TEXT NOT NULL,
                colorTag TEXT NOT NULL,
                note TEXT NOT NULL,
                semesterId INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO courses_new (id, name, teacher, room, days, startPeriod, endPeriod, weekRule, weeks, colorTag, note, semesterId)
            SELECT id, name, teacher, room, CAST(dayOfWeek AS TEXT), startPeriod, endPeriod, weekRule, weeks, colorTag, note, semesterId
            FROM courses
            """.trimIndent()
        )
        db.execSQL("DROP TABLE courses")
        db.execSQL("ALTER TABLE courses_new RENAME TO courses")
    }
}

/**
 * v2 -> v3: add the nullable custom-colour column to the courses table so the
 * colour-tag refactor can store a user-picked colour without losing data.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE courses ADD COLUMN customColorArgb INTEGER")
    }
}
