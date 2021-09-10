package ru.kpfu.weatherapp.gps

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import ru.kpfu.weatherapp.Consts
import ru.kpfu.weatherapp.MainViewModel


class GpsService : LifecycleService() {

    companion object {
        var running = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Consts.START_LOCATION_SERVICE -> {
                running = true
                startForeground()
                GpsListener.getInstance()
                    .startLocationListening(this.applicationContext) { l ->
                        MainViewModel.location.value = l
                    }
            }
            Consts.STOP_LOCATION_SERVICE -> {
                running = false
                GpsListener.getInstance().stopLocating()
                stopService(intent)
            }
            else -> {
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel() else ""

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        startForeground(Consts.FOREGROUND_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channel = NotificationChannel(
            Consts.CHANNEL_ID,
            Consts.CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW
        )
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return Consts.CHANNEL_ID
    }
}