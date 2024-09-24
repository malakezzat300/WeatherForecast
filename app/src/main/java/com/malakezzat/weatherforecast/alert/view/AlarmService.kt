package com.malakezzat.weatherforecast.alert.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import com.malakezzat.weatherforecast.R

class AlarmService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()

        // Start the overlay activity
        val overlayIntent = Intent(this, AlarmOverlayActivity::class.java)
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(overlayIntent)

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        return START_STICKY

//        createNotificationChannel()
//        val notification = createNotification()
//
//        startForeground(1, notification)
//
//        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
//        mediaPlayer.isLooping = true
//        mediaPlayer.start()
//
//        return START_STICKY
    }

    private fun createNotification(): Notification {
        val dismissIntent = Intent(this, AlarmReceiver::class.java)
        dismissIntent.action = "ACTION_DISMISS_ALARM" // Custom action to dismiss the alarm
        val dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return Notification.Builder(this, "alarm_channel_id")
            .setContentTitle("Alarm!")
            .setContentText("Press the button to dismiss.")
            .setSmallIcon(R.drawable.ic_alert)
            .addAction(Notification.Action.Builder(R.drawable.ic_add, "Dismiss", dismissPendingIntent).build())
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_MAX)
            .build()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel_id",
                "Alarm Channel",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Channel for alarm notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}