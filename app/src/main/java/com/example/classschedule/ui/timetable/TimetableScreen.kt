package com.example.classschedule.ui.timetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classschedule.domain.ScheduleCalculator
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.Semester
import com.example.classschedule.domain.model.WeekRule
import com.example.classschedule.ui.appContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateRangeFormatter = DateTimeFormatter.ofPattern("M月d日")
private val todayFormatter = DateTimeFormatter.ofPattern("M月d日")

@Composable
fun TimetableScreen(
    onAddCourse: () -> Unit,
    onEditCourse: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = appContainer()
    val viewModel: TimetableViewModel = viewModel(factory = TimetableViewModel.Factory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        TimetableContent(
            state = state,
            onAddCourse = onAddCourse,
            onCourseClick = { selectedCourse = it },
            onNextWeek = viewModel::nextWeek,
            onPreviousWeek = viewModel::previousWeek,
            onGoToCurrentWeek = viewModel::goToCurrentWeek,
            modifier = Modifier.fillMaxSize()
        )

        val selection = selectedCourse
        if (selection != null) {
            CourseDetailSheet(
                course = selection,
                onEdit = { onEditCourse(selection.id); selectedCourse = null },
                onDelete = { viewModel.deleteCourse(selection.id); selectedCourse = null },
                onDismiss = { selectedCourse = null }
            )
        }
    }
}

@Composable
private fun TimetableContent(
    state: TimetableUiState,
    onAddCourse: () -> Unit,
    onCourseClick: (Course) -> Unit,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onGoToCurrentWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        val semester = state.semester
        when {
            semester == null -> EmptyState("请先在「设置」中设置开学日期（学期设置）")
            else -> {
                WeekNavigator(
                    semester = semester,
                    week = state.currentWeek,
                    onNextWeek = onNextWeek,
                    onPreviousWeek = onPreviousWeek,
                    onGoToCurrentWeek = onGoToCurrentWeek
                )
                TodayCard(semester = semester, courses = state.courses)
                if (state.courses.isEmpty()) {
                    EmptyState(
                        message = "还没有课程，先添加第一门课吧",
                        onClick = onAddCourse
                    )
                } else {
                    val today = LocalDate.now().dayOfWeek.value
                    TimetableGrid(
                        semester = semester,
                        courses = state.courses,
                        currentWeek = state.currentWeek,
                        todayDayOfWeek = today,
                        onCourseClick = onCourseClick,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekNavigator(
    semester: Semester,
    week: Int,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onGoToCurrentWeek: () -> Unit
) {
    val (monday, sunday) = ScheduleCalculator.weekDateRange(semester.startDate, week)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) { Text("‹", style = MaterialTheme.typography.titleLarge) }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "第 $week 周",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${monday.format(dateRangeFormatter)} - ${sunday.format(dateRangeFormatter)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        IconButton(onClick = onNextWeek) { Text("›", style = MaterialTheme.typography.titleLarge) }
        TextButton(onClick = onGoToCurrentWeek) { Text("本周") }
    }
}

@Composable
private fun TodayCard(semester: Semester, courses: List<Course>) {
    val today = LocalDate.now()
    val todayWeek = ScheduleCalculator.weekOfDate(today, semester.startDate)
    val todayDay = today.dayOfWeek.value
    val inTerm = todayWeek in 1..semester.totalWeeks
    val todaysCourses = if (inTerm) {
        courses
            .filter { ScheduleCalculator.isCourseInWeek(it, todayWeek) && todayDay in it.days }
            .sortedBy { it.startPeriod }
    } else emptyList()

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "今天 ${today.format(todayFormatter)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            when {
                todayWeek == 0 -> Text("还没开学", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                !inTerm -> Text("本学期已结束", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                todaysCourses.isEmpty() -> Text("今天没有课", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                else -> todaysCourses.forEach { course ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "第${course.startPeriod}节",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(56.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (course.room.isNotBlank()) {
                            Text(
                                text = course.room,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailSheet(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(course.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            DetailRow("星期", daysToText(course.days))
            DetailRow("节次", "第 ${course.startPeriod} - ${course.endPeriod} 节")
            if (course.teacher.isNotBlank()) DetailRow("教师", course.teacher)
            if (course.room.isNotBlank()) DetailRow("教室", course.room)
            DetailRow("周次", weekRangeText(course))
            if (course.note.isNotBlank()) DetailRow("备注", course.note)

            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("编辑") }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) { Text("删除") }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

private fun dayLabel(day: Int): String = listOf("一", "二", "三", "四", "五", "六", "日")[(day - 1).coerceIn(0, 6)]

private fun daysToText(days: List<Int>): String =
    days.distinct().sorted().joinToString("、") { "周${dayLabel(it)}" }

private fun weekRangeText(course: Course): String = when (course.weekRule) {
    WeekRule.ALL -> "全部周"
    WeekRule.ODD -> "单数周"
    WeekRule.EVEN -> "双数周"
    WeekRule.RANGE -> "第 ${course.weeks.joinToString(", ")} 周"
}

@Composable
private fun EmptyState(message: String, onClick: (() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                if (onClick != null) {
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onClick) { Text("添加课程") }
                }
            }
        }
    }
}
