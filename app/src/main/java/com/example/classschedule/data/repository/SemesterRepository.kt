package com.example.classschedule.data.repository

import com.example.classschedule.data.local.SemesterDao
import com.example.classschedule.data.local.toDomain
import com.example.classschedule.data.local.toEntity
import com.example.classschedule.domain.model.Semester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for the semester definition. The MVP holds a single
 * semester (the first row); multi-semester switching is a later milestone.
 */
class SemesterRepository(private val semesterDao: SemesterDao) {

    fun observeFirst(): Flow<Semester?> = semesterDao.observeFirst().map { it?.toDomain() }

    suspend fun getFirst(): Semester? = semesterDao.getFirst()?.toDomain()

    suspend fun upsert(semester: Semester): Long = semesterDao.upsert(semester.toEntity())
}
