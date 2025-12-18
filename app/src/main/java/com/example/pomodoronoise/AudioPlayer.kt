package com.example.pomodoronoise

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class AudioPlayer(private val context: Context) { // 👈 接收 Context

    private val exoPlayer = ExoPlayer.Builder(context).build()

    fun play(soundResId: Int) {
        // ✅ 现在可以用 context
        val uri = Uri.parse("android.resource://${context.packageName}/$soundResId")
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
}