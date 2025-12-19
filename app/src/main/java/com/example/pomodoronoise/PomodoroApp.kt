package com.example.pomodoronoise

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PomodoroApp(
    application: Application = LocalContext.current.applicationContext as Application,
    viewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModelFactory(application))
) {
    val uiState by viewModel.uiState.collectAsState()

    PomodoroScreen(
        uiState = uiState,
        onStartStopClick = {
            if (uiState.isRunning) {
                viewModel.stopTimer()
            } else {
                viewModel.startTimer()
            }
        },
        onSwitchToWork = { viewModel.switchToWork() },
        onSwitchToRest = { viewModel.switchToRest() },
        onUpdateCurrentDuration = { viewModel.updateCurrentDuration(it) },
        onUpdateCurrentSound = { viewModel.updateCurrentSound(it) },
        onToggleInfiniteMode = { viewModel.toggleInfiniteMode() } // 添加这一行
    )
}

// PomodoroApp.kt
@Composable
fun PomodoroScreen(
    uiState: PomodoroUiState,
    onStartStopClick: () -> Unit,
    onSwitchToWork: () -> Unit,
    onSwitchToRest: () -> Unit,
    onUpdateCurrentDuration: (Int) -> Unit,
    onUpdateCurrentSound: (AudioPlayer.Sound) -> Unit,
    onToggleInfiniteMode: () -> Unit, // 添加这一行
    modifier: Modifier = Modifier
) {
    var openDialog by remember { mutableStateOf(false) }
    var openSoundDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // 模式指示器
        Text(
            text = if (uiState.currentMode == "work") "工作时间" else "休息时间",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (uiState.currentMode == "work") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 倒计时显示 - 添加点击功能
        Text(
            text = formatTime(uiState.timeRemaining),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                if (!uiState.isRunning) {
                    editMode = uiState.currentMode
                    // 将当前时间转换为分钟显示在输入框中
                    timeInput = (if (uiState.currentMode == "work")
                        uiState.workDuration else uiState.restDuration).toString()
                    openDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 控制按钮
        Row {
            Button(
                onClick = onSwitchToWork,
                enabled = !uiState.isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("工作")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSwitchToRest,
                enabled = !uiState.isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("休息")
            }
        }

        // 添加无限循环模式切换按钮
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onToggleInfiniteMode,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text(
                text = if (uiState.isInfiniteMode) "无限循环: 开启" else "无限循环: 关闭",
                color = MaterialTheme.colorScheme.surface,
            )
        }

        // 添加音声选择按钮
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { openSoundDialog = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text("音声: ${uiState.currentSound.displayName}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 开始/停止按钮
        Button(
            onClick = onStartStopClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text(
                text = if (uiState.isRunning) "停止番茄钟" else "开始番茄钟",
                fontSize = 18.sp
            )
        }
    }

    // 时间输入对话框
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text(if (editMode == "work") "设置工作时间(分钟)" else "设置休息时间(分钟)") },
            text = {
                TextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("分钟") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val minutes = timeInput.toIntOrNull()
                        if (minutes != null && minutes > 0) {
                            onUpdateCurrentDuration(minutes)
                        }
                        openDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 音声选择对话框
    if (openSoundDialog) {
        AlertDialog(
            onDismissRequest = { openSoundDialog = false },
            title = { Text("选择音声") },
            text = {
                Column {
                    AudioPlayer.Sound.values().forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateCurrentSound(sound)
                                    openSoundDialog = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = sound.displayName,
                                color = if (sound == uiState.currentSound)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { openSoundDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}