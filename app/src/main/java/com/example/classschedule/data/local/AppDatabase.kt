package com.example.classschedule.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CourseEntity::class, SemesterEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun semesterDao(): SemesterDao
}
