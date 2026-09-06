package com.example.classschedule.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classschedule.domain.model.ThemeMode
import com.example.classschedule.ui.appContainer
import kotlinx.coroutines.launch
import java.time.LocalDate

private val themeModes = listOf(
    ThemeMode.SYSTEM to "跟随系统",
    ThemeMode.LIGHT to "浅色",
    ThemeMode.DARK to "深色"
)

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val container = appContainer()
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf<String?>(null) }

    val semester = state.semester
    var name by remember(semester?.id) { mutableStateOf(semester?.name ?: "本学期") }
    var startDateText by remember(semester?.id) {
        mutableStateOf(semester?.startDate?.toString() ?: LocalDate.now().toString())
    }
    var totalWeeksText by remember(semester?.id) { mutableStateOf((semester?.totalWeeks ?: 16).toString()) }
    var periodsText by remember(semester?.id) { mutableStateOf((semester?.periodsPerDay ?: 8).toString()) }
    var semesterError by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = viewModel.exportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    message = "备份已导出"
                } catch (e: Exception) {
                    message = "导出失败：${e.message}"
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.readText() ?: ""
                    val ok = viewModel.importBackup(json)
                    message = if (ok) "导入成功" else "导入失败：文件格式不正确"
                } catch (e: Exception) {
                    message = "导入失败：${e.message}"
                }
            }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            message = "已保存"
            viewModel.resetSaved()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))
        Text("学期设置", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("学期名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("开学日期 (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = totalWeeksText,
                        onValueChange = { totalWeeksText = it },
                        label = { Text("总周数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = periodsText,
                        onValueChange = { periodsText = it },
                        label = { Text("每天节数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                semesterError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val start = try { LocalDate.parse(startDateText.trim()) }
                        catch (e: Exception) { semesterError = "开学日期格式应为 yyyy-MM-dd"; return@Button }
                        val total = totalWeeksText.trim().toIntOrNull()
                            ?: run { semesterError = "总周数需为数字"; return@Button }
                        val periods = periodsText.trim().toIntOrNull()
                            ?: run { semesterError = "每天节数需为数字"; return@Button }
                        semesterError = null
                        viewModel.saveSemester(name, start.toEpochDay(), total, periods)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("保存学期") }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("主题", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            themeModes.forEach { (mode, label) ->
                FilterChip(
                    selected = state.themeMode == mode,
                    onClick = { viewModel.setThemeMode(mode) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("动态取色（Android 12+）", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = state.dynamicColor, onCheckedChange = viewModel::setDynamicColor)
        }

        Spacer(Modifier.height(24.dp))
        Text("数据备份", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { exportLauncher.launch("课程表备份.json") },
                modifier = Modifier.weight(1f)
            ) { Text("导出备份") }
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                modifier = Modifier.weight(1f)
            ) { Text("导入备份") }
        }

        message?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(32.dp))
    }
}
