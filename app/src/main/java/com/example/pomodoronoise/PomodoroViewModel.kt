// PomodoroViewModel.kt
package com.example.pomodoronoise

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// PomodoroViewModel.kt
data class PomodoroUiState(
    val isRunning: Boolean = false,
    val currentMode: String = "work",
    val timeRemaining: Int = 25 * 60,
    val workDuration: Int = 25 * 60,
    val restDuration: Int = 5 * 60,
    val currentSound: AudioPlayer.Sound = AudioPlayer.Sound.RAIN, // 添加这行
    val isInfiniteMode: Boolean = false
)


class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState
    private val app = application

    private var timerJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun startTimer() {
        if (_uiState.value.isRunning) return

        // 启动前台服务（仅用于播放音频 + 保活通知）
        val mode = _uiState.value.currentMode
        val serviceIntent = Intent(app, PomodoroService::class.java).apply {
            putExtra("mode", mode)
            putExtra("sound", _uiState.value.currentSound.name) // 传递音声信息
        }
        app.startForegroundService(serviceIntent)

        // 启动倒计时协程（关键！）
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0) {
                delay(1000)
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }
            }
            // 时间到
            onTimerFinished()
        }

        _uiState.update { it.copy(isRunning = true) }
    }

    // PomodoroViewModel.kt
    private fun onTimerFinished() {
        // 获取当前状态
        val currentState = _uiState.value

        // 计算下一个模式
        val nextMode = if (currentState.currentMode == "work") "rest" else "work"
        val nextDuration = if (nextMode == "work") currentState.workDuration else currentState.restDuration

        // 更新状态并自动开始下一阶段
        _uiState.update {
            it.copy(
                currentMode = nextMode,
                timeRemaining = nextDuration
            )
        }

        // 自动开始下一个计时器
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0) {
                delay(1000)
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }
            }
            // 递归调用自身实现无限循环
            onTimerFinished()
        }
    }


    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null

        val serviceIntent = Intent(app, PomodoroService::class.java)
        app.stopService(serviceIntent)

        _uiState.update {
            it.copy(
                isRunning = false,
                timeRemaining = if (it.currentMode == "work") it.workDuration else it.restDuration
            )
        }
    }


    fun switchToWork() {
        if (!_uiState.value.isRunning) {
            _uiState.update {
                it.copy(currentMode = "work", timeRemaining = it.workDuration)
            }
        }
    }

    fun switchToRest() {
        if (!_uiState.value.isRunning) {
            _uiState.update {
                it.copy(currentMode = "rest", timeRemaining = it.restDuration)
            }
        }
    }

    fun updateWorkDuration(minutes: Int) {
        _uiState.update { it.copy(workDuration = minutes * 60) }
    }

    fun updateRestDuration(minutes: Int) {
        _uiState.update { it.copy(restDuration = minutes * 60) }
    }

    fun updateCurrentDuration(minutes: Int) {
        if (_uiState.value.currentMode == "work") {
            _uiState.update {
                it.copy(
                    workDuration = minutes * 60,
                    timeRemaining = if (!it.isRunning) minutes * 60 else it.timeRemaining
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    restDuration = minutes * 60,
                    timeRemaining = if (!it.isRunning) minutes * 60 else it.timeRemaining
                )
            }
        }
    }

    fun updateCurrentSound(sound: AudioPlayer.Sound) {
        _uiState.update { it.copy(currentSound = sound) }

        // 如果正在运行，更新服务中的音声
        if (_uiState.value.isRunning) {
            val serviceIntent = Intent(app, PomodoroService::class.java).apply {
                action = "UPDATE_SOUND"
                putExtra("sound", sound.name)
            }
            app.startService(serviceIntent)
        }
    }

    fun toggleInfiniteMode() {
        _uiState.update { it.copy(isInfiniteMode = !it.isInfiniteMode) }
    }
}