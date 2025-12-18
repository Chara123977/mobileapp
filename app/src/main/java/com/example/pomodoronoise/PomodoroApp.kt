package com.example.pomodoronoise

import com.example.pomodoronoise.formatTime
import android.app.Application
import android.content.Context
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
        onUpdateCurrentDuration = { viewModel.updateCurrentDuration(it) } // 修改这一行
    )
}

@Composable
fun PomodoroScreen(
    uiState: PomodoroUiState,
    onStartStopClick: () -> Unit,
    onSwitchToWork: () -> Unit,
    onSwitchToRest: () -> Unit,
    onUpdateCurrentDuration: (Int) -> Unit, // 修改这一行
    modifier: Modifier = Modifier
) {
    var openDialog by remember { mutableStateOf(false) }
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
                            onUpdateCurrentDuration(minutes) // 修改这一行
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
}


//@Composable
//fun formatTime(totalSeconds: Int): String {
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return String.format("%02d:%02d", minutes, seconds)
//}