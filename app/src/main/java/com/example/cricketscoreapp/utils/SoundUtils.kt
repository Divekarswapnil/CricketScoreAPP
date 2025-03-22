package com.example.cricketscoreapp.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.cricketscoreapp.R

object SoundUtils {
    private var mediaPlayer: MediaPlayer? = null

    // Function to play sound
    fun playSound(context: Context, soundResId: Int) {
        mediaPlayer?.release() // Release if already playing
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.start()
    }

    // Call this function based on match result
    fun handleMatchResult(context: Context, result: String) {
        when (result) {
            "win" -> playSound(context, R.raw.win)   // Play win sound
            "lose" -> playSound(context, R.raw.lose) // Play lose sound
            "tie" -> playSound(context, R.raw.tie)   // Play tie sound
            "bgm" -> playSound(context, R.raw.match_game_background_intro_theme)   // Play tie sound
        }
    }

    // Release MediaPlayer when no longer needed
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
