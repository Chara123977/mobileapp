package com.example.pomodoronoise

import android.app.*
import android.os.VibratorManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

const val CHANNEL_ID = "PomodoroTimerChannel"
const val NOTIFICATION_ID = 1

class PomodoroService : Service() {

    private lateinit var audioPlayer: AudioPlayer
    private var timerJob: Job? = null
    private var currentSound: AudioPlayer.Sound = AudioPlayer.Sound.RAIN

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioPlayer = AudioPlayer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "UPDATE_SOUND" -> {
                val soundName = intent.getStringExtra("sound")
                val sound = AudioPlayer.Sound.valueOf(soundName ?: "RAIN")
                currentSound = sound
                audioPlayer.release()
                audioPlayer = AudioPlayer(this)
                audioPlayer.play(sound)
                return START_NOT_STICKY
            }
        }

        val mode = intent?.getStringExtra("mode") ?: "work"
        val soundName = intent?.getStringExtra("sound")
        currentSound = try {
            AudioPlayer.Sound.valueOf(soundName ?: "RAIN")
        } catch (e: IllegalArgumentException) {
            AudioPlayer.Sound.RAIN
        }

        startForeground(NOTIFICATION_ID, createNotification(mode))
        audioPlayer.play(currentSound)

        return START_NOT_STICKY
    }

// 移除 onTimerFinished() 中的 stopSelf()
// 改为：外部调用 stopService 时 onDestroy 会被触发

    private fun onTimerFinished() {
        // 震动提醒
        vibrateDevice()

        // 停止音频
        audioPlayer.release()

        // 停止自身服务
        stopSelf()

    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        audioPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ———————— 工具方法 ————————

    private fun createNotification(mode: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("专注中...")
            .setContentText(if (mode == "work") "工作时间" else "休息时间")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄钟计时",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于后台计时和白噪声播放"
                enableVibration(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun vibrateDevice() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }
}