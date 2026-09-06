package com.example.classschedule.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.classschedule.domain.ScheduleCalculator
import com.example.classschedule.domain.model.Course
import com.example.classschedule.domain.model.Semester
import com.example.classschedule.ui.theme.courseBlockColors

private val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

/**
 * The weekly timetable grid: a fixed day header row over a vertically scrollable
 * body with a period-time gutter and one column per weekday. Course blocks are
 * placed by their day/period span and coloured by their [Course.colorTag].
 */
@Composable
fun TimetableGrid(
    semester: Semester,
    courses: List<Course>,
    currentWeek: Int,
    todayDayOfWeek: Int,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val periodCount = semester.periodsPerDay.coerceAtLeast(1)
    val visibleCourses = courses.filter { ScheduleCalculator.isCourseInWeek(it, currentWeek) }

    val rowHeight = 60.dp
    val headerHeight = 34.dp
    val gutterWidth = 64.dp

    BoxWithConstraints(modifier = modifier) {
        val dayWidth = (maxWidth - gutterWidth) / 7

        Column(modifier = Modifier.fillMaxSize()) {
            DayHeaderRow(
                gutterWidth = gutterWidth,
                dayWidth = dayWidth,
                headerHeight = headerHeight,
                todayDayOfWeek = todayDayOfWeek
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight * periodCount)
                ) {
                    PeriodGutter(
                        semester = semester,
                        periodCount = periodCount,
                        width = gutterWidth,
                        rowHeight = rowHeight
                    )
                    for (day in 1..7) {
                        val dayCourses = visibleCourses
                            .filter { day in it.days }
                            .sortedBy { it.startPeriod }
                        DayColumn(
                            dayCourses = dayCourses,
                            width = dayWidth,
                            rowHeight = rowHeight,
                            periodCount = periodCount,
                            isToday = day == todayDayOfWeek,
                            onCourseClick = onCourseClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeaderRow(
    gutterWidth: Dp,
    dayWidth: Dp,
    headerHeight: Dp,
    todayDayOfWeek: Int
) {
    Row(modifier = Modifier.height(headerHeight).fillMaxWidth()) {
        Spacer(modifier = Modifier.width(gutterWidth))
        for (day in 1..7) {
            val isToday = day == todayDayOfWeek
            Box(
                modifier = Modifier.width(dayWidth).height(headerHeight),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayLabels[day - 1],
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodGutter(
    semester: Semester,
    periodCount: Int,
    width: Dp,
    rowHeight: Dp
) {
    Column(modifier = Modifier.width(width)) {
        for (period in 1..periodCount) {
            val time = semester.periodTimes.getOrNull(period - 1)
            Box(
                modifier = Modifier.fillMaxWidth().height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$period", style = MaterialTheme.typography.labelMedium)
                    if (time != null) {
                        Text(
                            text = "${time.start}-${time.end}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    dayCourses: List<Course>,
    width: Dp,
    rowHeight: Dp,
    periodCount: Int,
    isToday: Boolean,
    onCourseClick: (Course) -> Unit
) {
    Column(
        modifier = Modifier
            .width(width)
            .height(rowHeight * periodCount)
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
            )
    ) {
        var currentPeriod = 1
        for (course in dayCourses) {
            val gapPeriods = course.startPeriod - currentPeriod
            if (gapPeriods > 0) {
                Spacer(modifier = Modifier.height(rowHeight * gapPeriods))
            }
            CourseBlock(
                course = course,
                width = width,
                height = rowHeight * course.periodCount,
                onClick = { onCourseClick(course) }
            )
            currentPeriod = course.endPeriod + 1
        }
        val tailPeriods = periodCount - (currentPeriod - 1)
        if (tailPeriods > 0) {
            Spacer(modifier = Modifier.height(rowHeight * tailPeriods))
        }
    }
}

@Composable
private fun CourseBlock(course: Course, width: Dp, height: Dp, onClick: () -> Unit) {
    val colors = courseBlockColors(course)
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                color = colors.content,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (course.room.isNotBlank()) {
                Text(
                    text = course.room,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.content,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
