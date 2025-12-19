// AudioPlayer.kt
package com.example.pomodoronoise

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import androidx.core.net.toUri

class AudioPlayer(private val context: Context) {
    private val exoPlayer = ExoPlayer.Builder(context).build()

    // 定义可用的音声选项
    enum class Sound(val resourceId: Int, val displayName: String) {
        RAIN(R.raw.rain, "🌧️ 雨声"),
        OCEAN(R.raw.cafe, "☕️ 咖啡馆"), // 假设你有这些资源文件
        FOREST(R.raw.forest, "🐦 森林鸟鸣"),
        BROWN_NOISE(R.raw.waves, "🌊 海浪")
    }

    private var currentSound: Sound = Sound.RAIN

    fun play(sound: Sound = currentSound) {
        // 如果正在播放且声音相同，则不重新加载
        if (exoPlayer.isPlaying && sound == currentSound) return

        currentSound = sound
        val uri = "android.resource://${context.packageName}/${sound.resourceId}".toUri()
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.repeatMode = com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun release() {
        exoPlayer.release()
    }

    fun getCurrentSound(): Sound = currentSound
}
