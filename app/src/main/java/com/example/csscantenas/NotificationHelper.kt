package com.example.csscantenas

import android.app.*
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "antenna_monitor_channel"
    private const val CHANNEL_NAME = "Monitoreo de Antenas"
    private const val TAG = "CSSCAntenas"

    fun createServiceNotification(context: Context): Notification {
        createNotificationChannel(context)
        Log.d(TAG, "Creando notificación de servicio para monitoreo activo")
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("CSSC Antenas")
            .setContentText("Monitoreo activo")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500))
            .build().also {
                Log.d(TAG, "Notificación de servicio creada y lista")
            }
    }

    fun showAntennaAlert(context: Context, title: String, message: String) {
        val notificationId = System.currentTimeMillis().toInt()
        Log.d(TAG, "Intentando mostrar alerta: Título='$title', Mensaje='$message', ID=$notificationId")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notificación de alerta enviada con ID=$notificationId")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creando canal de notificación si no existe")
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de monitoreo de antenas"
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado con ID=$CHANNEL_ID")
        } else {
            Log.d(TAG, "Versión de Android inferior a Oreo, no se crea canal")
        }
    }
}