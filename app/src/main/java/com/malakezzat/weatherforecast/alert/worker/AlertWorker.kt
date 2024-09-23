package com.malakezzat.weatherforecast.alert.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.Priority
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.alert.viewmodel.AlertViewModel
import com.malakezzat.weatherforecast.alert.viewmodel.AlertViewModelFactory
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.home.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.databinding.FragmentAlertBinding
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.TimeUnit

class AlertWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    val context: Context = appContext

    private lateinit var repository: WeatherRepository

    override suspend fun doWork(): Result {
        return try {
            repository = WeatherRepositoryImpl(
                WeatherRemoteDataSourceImpl.getInstance(),
                WeatherLocalDataSourceImpl(AppDatabase.getInstance(context))
            )

            val id = inputData.getString(context.getString(R.string.id_worker)).toString()
            Log.i("deleteAfterWork", "doWork: id $id")
            val message = inputData.getString(context.getString(R.string.message_worker)).toString()
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


        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_alert)
            .setAutoCancel(true)
            //.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        notificationManager.notify(1, notification)
    }


}
