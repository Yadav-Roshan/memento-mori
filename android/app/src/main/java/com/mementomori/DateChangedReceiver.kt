package com.mementomori

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.Calendar

class DateChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_DATE_CHANGED) {
            val prefs = context.getSharedPreferences("memento_mori", Context.MODE_PRIVATE)
            val birthdate = prefs.getLong("birthdate", 0L)

            if (birthdate > 0) {
                val birthCalendar = Calendar.getInstance().apply { timeInMillis = birthdate }
                val todayCalendar = Calendar.getInstance()

                val birthMonth = birthCalendar.get(Calendar.MONTH)
                val birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH)
                val todayMonth = todayCalendar.get(Calendar.MONTH)
                val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

                if (birthMonth == todayMonth && birthDay == todayDay) {
                    showBirthdayNotification(context)
                }
            }
        }
    }

    private fun showBirthdayNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "birthday_channel"
        val channelName = "Birthday Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_skull)
            .setContentTitle("A Reminder")
            .setContentText("Another year closer to the end. Make it count. Happy Birthday.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }
}
