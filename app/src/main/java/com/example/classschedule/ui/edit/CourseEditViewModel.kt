package com.example.classschedule.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.local.SettingsDataStore
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import com.example.classschedule.di.AppContainer
import com.example.classschedule.domain.ScheduleCalculator
import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.WeekRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseEditUiState(
    val courseId: Long = 0,
    val name: String = "",
    val teacher: String = "",
    val room: String = "",
    val days: List<Int> = listOf(1),
    val startPeriod: Int = 1,
    val endPeriod: Int = 2,
    val maxPeriods: Int = 8,
    val weekRule: WeekRule = WeekRule.ALL,
    val weeksText: String = "",
    val colorTag: ColorTag = ColorTag.BLUE,
    val customColorArgb: Long? = null,
    val note: String = "",
    val loading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

/**
 * Holds the course add/edit form and persists it. [courseId] is 0 for a new
 * course, otherwise loads the existing one for editing.
 */
class CourseEditViewModel(
    private val courseRepository: CourseRepository,
    private val semesterRepository: SemesterRepository,
    private val settingsDataStore: SettingsDataStore,
    private val courseId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(
        if (courseId > 0) CourseEditUiState(courseId = courseId, loading = true) else CourseEditUiState()
    )
    val state: StateFlow<CourseEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // The maximum selectable period follows the semester's daily period count.
            val maxPeriods = semesterRepository.getFirst()?.periodsPerDay?.coerceIn(1, 12) ?: 8
            when {
                courseId > 0 -> {
                    val course = courseRepository.getById(courseId)
                    if (course != null) {
                        _state.value = CourseEditUiState(
                            courseId = course.id,
                            name = course.name,
                            teacher = course.teacher,
                            room = course.room,
                            days = course.days,
                            startPeriod = course.startPeriod.coerceIn(1, maxPeriods),
                            endPeriod = course.endPeriod.coerceIn(1, maxPeriods),
                            maxPeriods = maxPeriods,
                            weekRule = course.weekRule,
                            weeksText = course.weeks.joinToString(","),
                            colorTag = course.colorTag,
                            customColorArgb = course.customColorArgb,
                            note = course.note,
                            loading = false
                        )
                    } else {
                        _state.update { it.copy(loading = false, error = "未找到该课程", maxPeriods = maxPeriods) }
                    }
                }
                else -> {
                    _state.update {
                        it.copy(
                            maxPeriods = maxPeriods,
                            startPeriod = it.startPeriod.coerceIn(1, maxPeriods),
                            endPeriod = it.endPeriod.coerceIn(1, maxPeriods)
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = update { it.copy(name = value, error = null) }
    fun onTeacherChange(value: String) = update { it.copy(teacher = value) }
    fun onRoomChange(value: String) = update { it.copy(room = value) }
    fun toggleDay(day: Int) = update {
        val current = it.days
        val next = if (day in current) current - day else current + day
        it.copy(days = next.distinct().sorted(), error = null)
    }
    fun onStartPeriodChange(value: Int) = update {
        val start = value.coerceIn(1, it.maxPeriods)
        it.copy(startPeriod = start, endPeriod = it.endPeriod.coerceIn(start, it.maxPeriods), error = null)
    }
    fun onEndPeriodChange(value: Int) = update {
        it.copy(endPeriod = value.coerceIn(it.startPeriod, it.maxPeriods), error = null)
    }
    fun onWeekRuleChange(value: WeekRule) = update { it.copy(weekRule = value) }
    fun onWeeksTextChange(value: String) = update { it.copy(weeksText = value, error = null) }
    fun onColorTagChange(value: ColorTag) = update {
        it.copy(colorTag = value, customColorArgb = if (value == ColorTag.CUSTOM) it.customColorArgb else null, error = null)
    }
    fun onCustomColorChange(argb: Long) = update {
        it.copy(colorTag = ColorTag.CUSTOM, customColorArgb = argb, error = null)
    }
    fun onNoteChange(value: String) = update { it.copy(note = value) }

    private fun update(transform: (CourseEditUiState) -> CourseEditUiState) = _state.update(transform)

    fun save() {
        val s = _state.value
        when {
            s.name.isBlank() -> { setError("请输入课程名称"); return }
            s.days.isEmpty() -> { setError("请至少选择一天上课"); return }
            s.startPeriod > s.endPeriod -> { setError("结束节不能早于开始节"); return }
            s.endPeriod > s.maxPeriods -> { setError("节次不能超过每天 ${s.maxPeriods} 节"); return }
            s.weekRule == WeekRule.RANGE && ScheduleCalculator.parseWeekList(s.weeksText).isEmpty() -> {
                setError("自定义周次需至少填写一个周次（如 1,3,5 或 1-8）")
                return
            }
        }
        viewModelScope.launch {
            val semesterId = settingsDataStore.settings.first().currentSemesterId
            courseRepository.upsert(
                Course(
                    id = s.courseId,
                    name = s.name.trim(),
                    teacher = s.teacher.trim(),
                    room = s.room.trim(),
                    days = s.days.distinct().sorted(),
                    startPeriod = s.startPeriod,
                    endPeriod = s.endPeriod,
                    weekRule = s.weekRule,
                    weeks = ScheduleCalculator.parseWeekList(s.weeksText),
                    colorTag = s.colorTag,
                    customColorArgb = if (s.colorTag == ColorTag.CUSTOM) s.customColorArgb else null,
                    note = s.note.trim(),
                    semesterId = semesterId
                )
            )
            _state.update { it.copy(saved = true) }
        }
    }

    fun delete() {
        if (courseId <= 0) return
        viewModelScope.launch {
            courseRepository.deleteById(courseId)
            _state.update { it.copy(saved = true) }
        }
    }

    private fun setError(message: String) = _state.update { it.copy(error = message) }

    /** Manual DI factory carrying the target [courseId]. */
    class Factory(
        private val container: AppContainer,
        private val courseId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CourseEditViewModel(
            courseRepository = container.courseRepository,
            semesterRepository = container.semesterRepository,
            settingsDataStore = container.settingsDataStore,
            courseId = courseId
        ) as T
    }
}
