package com.example.classschedule.di

import android.content.Context
import androidx.room.Room
import com.example.classschedule.data.BackupManager
import com.example.classschedule.data.local.AppDatabase
import com.example.classschedule.data.local.MIGRATION_1_2
import com.example.classschedule.data.local.MIGRATION_2_3
import com.example.classschedule.data.local.SettingsDataStore
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository

/**
 * Manual dependency-injection container. Owns database + repositories and is
 * created once in [ClassScheduleApp]. Intentionally lightweight (no Hilt) for
 * the MVP; can be swapped for a DI framework later.
 */
class AppContainer(context: Context) {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "classschedule.db"
    )
        // Preserve user data: migrate the schema instead of dropping the DB.
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        // Only fall back to a destructive rebuild for future/unhandled version jumps.
        .fallbackToDestructiveMigration()
        .build()

    val settingsDataStore = SettingsDataStore(context.applicationContext)
    val courseRepository = CourseRepository(database.courseDao())
    val semesterRepository = SemesterRepository(database.semesterDao())
    val backupManager = BackupManager(courseRepository, semesterRepository)
}
