package com.malakezzat.weatherforecast.alert.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.alert.view.AlarmService
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl

class AlertWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    val context: Context = appContext
    lateinit var message : String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var repository: WeatherRepository

    override suspend fun doWork(): Result {
        return try {
            sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.my_preference),
                Context.MODE_PRIVATE)

            repository = WeatherRepositoryImpl(
                WeatherRemoteDataSourceImpl.getInstance(),
                WeatherLocalDataSourceImpl(AppDatabase.getInstance(context))
            )

            val id = inputData.getString(context.getString(R.string.id_worker)).toString()
            Log.i("deleteAfterWork", "doWork: id $id")
            message = inputData.getString(context.getString(R.string.message_worker)).toString()
            val type = inputData.getInt(context.getString(R.string.type_worker),0)
            sendNotification(message,type)
            repository.deleteAlertById(id)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendNotification(message : String,type : Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.channel_id)

        val channel =
            NotificationChannel(channelId, context.getString(R.string.channel), NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        if(sharedPreferences.getBoolean(context.getString(R.string.enable_pref),false)){
        if(type == R.string.notification) {
            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_cloud)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .build()
            notificationManager.notify(1, notification)
        } else {
            val intent = Intent(applicationContext, AlarmService::class.java)
            intent.putExtra(context.getString(R.string.message_worker),message)
            context.startService(intent)
        }
    }

    }


}
