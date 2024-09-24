package com.malakezzat.weatherforecast.alert.view

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.malakezzat.weatherforecast.R

class AlarmOverlayActivity : Activity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set full-screen layout
        setContentView(R.layout.activity_alarm_overlay)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        // Set up dismiss button
        val dismissButton: Button = findViewById(R.id.dismiss_button)
        dismissButton.setOnClickListener {
            dismissAlarm()
        }
    }

    private fun dismissAlarm() {
        // Stop media player
        mediaPlayer.stop()
        mediaPlayer.release()

        finish()
        // Stop the service if it is running
        stopService(Intent(this, AlarmService::class.java))

        // Finish the overlay activity

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}