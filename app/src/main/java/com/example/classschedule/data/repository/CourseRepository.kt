package com.example.classschedule.data.repository

import com.example.classschedule.data.local.CourseDao
import com.example.classschedule.data.local.toDomain
import com.example.classschedule.data.local.toEntity
import com.example.classschedule.domain.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for courses. Wraps [CourseDao] and maps Room entities
 * to/from domain [Course] objects.
 */
class CourseRepository(private val courseDao: CourseDao) {

    fun observeCourses(semesterId: Long): Flow<List<Course>> =
        courseDao.observeCourses(semesterId).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Course? = courseDao.getById(id)?.toDomain()

    suspend fun upsert(course: Course): Long = courseDao.upsert(course.toEntity())

    suspend fun delete(course: Course) = courseDao.delete(course.toEntity())

    suspend fun deleteById(id: Long) = courseDao.deleteById(id)
}
