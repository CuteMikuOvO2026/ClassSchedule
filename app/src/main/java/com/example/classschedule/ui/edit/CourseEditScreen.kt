package com.example.classschedule.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.WeekRule
import com.example.classschedule.ui.appContainer
import com.example.classschedule.ui.theme.DARK_TAGS
import com.example.classschedule.ui.theme.LIGHT_TAGS
import com.example.classschedule.ui.theme.presetSwatchColor

private val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
private val weekRuleOptions = listOf(
    WeekRule.ALL to "全部",
    WeekRule.ODD to "单数周",
    WeekRule.EVEN to "双数周",
    WeekRule.RANGE to "自定义"
)

@Composable
fun CourseEditScreen(
    courseId: Long,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = appContainer()
    val viewModel: CourseEditViewModel = viewModel(
        factory = CourseEditViewModel.Factory(container, courseId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showColorPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Column(modifier = modifier.fillMaxSize()) {
        EditHeader(
            title = if (courseId > 0) "编辑课程" else "添加课程",
            onBack = onDone,
            onDelete = if (courseId > 0) ({ viewModel.delete() }) else null
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("课程名称") },
                isError = state.error != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.teacher,
                onValueChange = viewModel::onTeacherChange,
                label = { Text("教师") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.room,
                onValueChange = viewModel::onRoomChange,
                label = { Text("教室") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("星期（可多选）", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (day in 1..7) {
                    FilterChip(
                        selected = day in state.days,
                        onClick = { viewModel.toggleDay(day) },
                        label = { Text(dayLabels[day - 1]) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("节次", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    text = "每天最多 ${state.maxPeriods} 节",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PeriodStepper(
                    label = "开始",
                    value = state.startPeriod,
                    onIncrease = { viewModel.onStartPeriodChange(state.startPeriod + 1) },
                    onDecrease = { viewModel.onStartPeriodChange(state.startPeriod - 1) },
                    modifier = Modifier.weight(1f),
                    enableIncrease = state.startPeriod < state.maxPeriods,
                    enableDecrease = state.startPeriod > 1
                )
                Spacer(Modifier.width(12.dp))
                PeriodStepper(
                    label = "结束",
                    value = state.endPeriod,
                    onIncrease = { viewModel.onEndPeriodChange(state.endPeriod + 1) },
                    onDecrease = { viewModel.onEndPeriodChange(state.endPeriod - 1) },
                    modifier = Modifier.weight(1f),
                    enableIncrease = state.endPeriod < state.maxPeriods,
                    enableDecrease = state.endPeriod > state.startPeriod
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("周次规则", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekRuleOptions.forEach { (rule, label) ->
                    FilterChip(
                        selected = state.weekRule == rule,
                        onClick = { viewModel.onWeekRuleChange(rule) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (state.weekRule == WeekRule.RANGE) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.weeksText,
                    onValueChange = viewModel::onWeeksTextChange,
                    label = { Text("周次（如 1,3,5 或 1-8）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("颜色标签", style = MaterialTheme.typography.titleMedium)
            // Row 1: four light pastel colours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LIGHT_TAGS.forEach { tag ->
                    ColorSwatch(
                        tag = tag,
                        selected = state.colorTag == tag,
                        onClick = { viewModel.onColorTagChange(tag) }
                    )
                }
            }
            // Row 2: three deep colours + one custom colour
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DARK_TAGS.forEach { tag ->
                    ColorSwatch(
                        tag = tag,
                        selected = state.colorTag == tag,
                        onClick = { viewModel.onColorTagChange(tag) }
                    )
                }
                CustomColorSwatch(
                    selected = state.colorTag == ColorTag.CUSTOM,
                    argb = state.customColorArgb,
                    onClick = { showColorPicker = true }
                )
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("备注") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("保存")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onDismiss = { showColorPicker = false },
            onColorChosen = { argb ->
                viewModel.onCustomColorChange(argb)
                showColorPicker = false
            }
        )
    }
}

@Composable
private fun EditHeader(
    title: String,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.titleLarge) }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (onDelete != null) {
            TextButton(onClick = onDelete) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PeriodStepper(
    label: String,
    value: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    enableIncrease: Boolean = true,
    enableDecrease: Boolean = true
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onDecrease, enabled = enableDecrease) { Text("－") }
        Text("第 $value 节", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        TextButton(onClick = onIncrease, enabled = enableIncrease) { Text("＋") }
    }
}

@Composable
private fun ColorSwatch(tag: ColorTag, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(presetSwatchColor(tag))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun CustomColorSwatch(selected: Boolean, argb: Long?, onClick: () -> Unit) {
    val brush = if (argb != null) {
        Brush.sweepGradient(listOf(Color(argb.toInt()), Color(argb.toInt())))
    } else {
        Brush.sweepGradient(
            listOf(
                Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176), Color(0xFF81C784),
                Color(0xFF4FC3F7), Color(0xFF9575CD), Color(0xFFF06292)
            )
        )
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(brush)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

private val customPickerColors: List<Long> = listOf(
    0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1, 0xFF3949AB, 0xFF1E88E5, 0xFF039BE5,
    0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFC0CA33, 0xFFFDD835, 0xFFFFB300,
    0xFFFB8C00, 0xFFF4511E, 0xFF6D4C41, 0xFF757575, 0xFF546E7A, 0xFFB71C1C, 0xFF880E4F,
    0xFF4A148C, 0xFF1A237E, 0xFF0D47A1, 0xFF006064, 0xFF004D40, 0xFF1B5E20, 0xFF33691E,
    0xFF827717, 0xFFF57F17, 0xFFE65100, 0xFFBF360C
)

@Composable
private fun ColorPickerDialog(onDismiss: () -> Unit, onColorChosen: (Long) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("自定义颜色", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                customPickerColors.chunked(8).forEach { rowColors ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowColors.forEach { argb ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(argb.toInt()))
                                    .clickable { onColorChosen(argb) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("取消") }
            }
        }
    }
}
