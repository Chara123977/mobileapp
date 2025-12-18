package com.example.pomodoronoise

import com.example.pomodoronoise.formatTime
import android.app.Application
import android.content.Context
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
        onSwitchToRest = { viewModel.switchToRest() }
    )
}

@Composable
fun PomodoroScreen(
    uiState: PomodoroUiState,
    onStartStopClick: () -> Unit,
    onSwitchToWork: () -> Unit,
    onSwitchToRest: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // 倒计时显示
        Text(
            text = formatTime(uiState.timeRemaining),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
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
}

//@Composable
//fun formatTime(totalSeconds: Int): String {
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return String.format("%02d:%02d", minutes, seconds)
//}