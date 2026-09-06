package com.example.classschedule.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.local.SettingsDataStore
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import com.example.classschedule.di.AppContainer
import com.example.classschedule.domain.ScheduleCalculator
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.Semester
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TimetableUiState(
    val semester: Semester? = null,
    val courses: List<Course> = emptyList(),
    val currentWeek: Int = 1,
    val loading: Boolean = true
)

/**
 * Drives the timetable screen: observes the semester + its courses and the
 * user's selected week, and exposes week-navigation actions. Courses are kept
 * untrimmed; week filtering is done by the UI via [ScheduleCalculator.isCourseInWeek].
 */
class TimetableViewModel(
    private val courseRepository: CourseRepository,
    semesterRepository: SemesterRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TimetableUiState> = semesterRepository.observeFirst()
        .flatMapLatest { semester ->
            val semesterId = semester?.id ?: 1L
            combine(
                courseRepository.observeCourses(semesterId),
                settingsDataStore.settings
            ) { courses, settings ->
                TimetableUiState(
                    semester = semester,
                    courses = courses,
                    currentWeek = settings.currentWeek,
                    loading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimetableUiState()
        )

    fun nextWeek() = shiftWeek(1)

    fun previousWeek() = shiftWeek(-1)

    private fun shiftWeek(delta: Int) {
        val current = uiState.value.currentWeek
        val newWeek = current + delta
        if (newWeek >= 1) {
            viewModelScope.launch { settingsDataStore.setCurrentWeek(newWeek) }
        }
    }

    /** Jump back to whichever week today's date falls in. */
    fun goToCurrentWeek() {
        val start = uiState.value.semester?.startDate ?: return
        val week = ScheduleCalculator.weekOfDate(LocalDate.now(), start).coerceAtLeast(1)
        viewModelScope.launch { settingsDataStore.setCurrentWeek(week) }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch { courseRepository.deleteById(id) }
    }

    /** Manual DI factory (no Hilt) that pulls dependencies from [AppContainer]. */
    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TimetableViewModel(
            courseRepository = container.courseRepository,
            semesterRepository = container.semesterRepository,
            settingsDataStore = container.settingsDataStore
        ) as T
    }
}
