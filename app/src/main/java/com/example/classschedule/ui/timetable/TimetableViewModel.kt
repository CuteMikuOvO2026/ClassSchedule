package com.example.classschedule.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import com.example.classschedule.di.AppContainer
import com.example.classschedule.domain.ScheduleCalculator
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.Semester
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    semesterRepository: SemesterRepository
) : ViewModel() {

    /**
     * Week currently shown, or `null` while the screen follows today's date.
     *
     * Deliberately in-memory only (never persisted): a fresh ViewModel starts at
     * `null`, so opening the app always lands on the week containing today instead
     * of restoring the week the user last browsed.
     */
    private val selectedWeek = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TimetableUiState> = combine(
        semesterRepository.observeFirst(),
        selectedWeek
    ) { semester, week -> semester to week }
        .flatMapLatest { (semester, week) ->
            val semesterId = semester?.id ?: 1L
            courseRepository.observeCourses(semesterId).map { courses ->
                TimetableUiState(
                    semester = semester,
                    courses = courses,
                    currentWeek = week ?: semester?.let { ScheduleCalculator.currentWeek(it) } ?: 1,
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
        val newWeek = uiState.value.currentWeek + delta
        if (newWeek >= 1) selectedWeek.value = newWeek
    }

    /** Drop the manual selection so the grid snaps back to today's week. */
    fun goToCurrentWeek() {
        selectedWeek.value = null
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch { courseRepository.deleteById(id) }
    }

    /** Manual DI factory (no Hilt) that pulls dependencies from [AppContainer]. */
    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TimetableViewModel(
            courseRepository = container.courseRepository,
            semesterRepository = container.semesterRepository
        ) as T
    }
}
