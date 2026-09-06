package com.example.classschedule.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.BackupManager
import com.example.classschedule.data.local.SettingsDataStore
import com.example.classschedule.data.repository.CourseRepository
import com.example.classschedule.data.repository.SemesterRepository
import com.example.classschedule.di.AppContainer
import com.example.classschedule.domain.model.Semester
import com.example.classschedule.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SettingsUiState(
    val semester: Semester? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val loading: Boolean = true,
    val saved: Boolean = false
)

class SettingsViewModel(
    private val semesterRepository: SemesterRepository,
    private val settingsDataStore: SettingsDataStore,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(semesterRepository.observeFirst(), settingsDataStore.settings) { semester, settings ->
                SettingsUiState(
                    semester = semester,
                    themeMode = settings.themeMode,
                    dynamicColor = settings.dynamicColor,
                    loading = false
                )
            }.collect { _state.value = it }
        }
    }

    fun saveSemester(name: String, startDateEpochDay: Long, totalWeeks: Int, periodsPerDay: Int) {
        viewModelScope.launch {
            val current = _state.value.semester
            semesterRepository.upsert(
                Semester(
                    id = current?.id ?: 1,
                    name = name.ifBlank { "本学期" },
                    startDate = LocalDate.ofEpochDay(startDateEpochDay),
                    totalWeeks = totalWeeks.coerceIn(1, 30),
                    periodsPerDay = periodsPerDay.coerceIn(1, 12)
                )
            )
            _state.update { it.copy(saved = true) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setDynamicColor(enabled) }
    }

    suspend fun exportBackup(): String = backupManager.exportJson()

    suspend fun importBackup(json: String): Boolean = backupManager.importFromJson(json)

    fun resetSaved() = _state.update { it.copy(saved = false) }

    /** Cached current semester id (used by other components if needed). */
    val currentSemesterId: Long
        get() = _state.value.semester?.id ?: 1

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(
            semesterRepository = container.semesterRepository,
            settingsDataStore = container.settingsDataStore,
            backupManager = container.backupManager
        ) as T
    }
}
