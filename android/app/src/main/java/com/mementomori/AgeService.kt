package com.mementomori

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AgeService : Service() {
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var notificationManager: NotificationManager
    private var birthdate: Long = 0L
    
    companion object {
        const val CHANNEL_ID = "memento_mori_channel"
        const val NOTIFICATION_ID = 1
        var isRunning = false
    }
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, 1000)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("memento_mori", MODE_PRIVATE)
        birthdate = prefs.getLong("birthdate", 0L)
        
        if (birthdate == 0L) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        isRunning = true
        startForeground(NOTIFICATION_ID, createNotification())
        handler.post(updateRunnable)
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(updateRunnable)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Memento Mori",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows your age in real-time"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun calculateAge(): String {
        val now = System.currentTimeMillis()
        val ageMillis = now - birthdate
        
        val birthCalendar = Calendar.getInstance().apply { timeInMillis = birthdate }
        val nowCalendar = Calendar.getInstance()
        
        var years = nowCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        
        // Check if birthday hasn't occurred yet this year
        val birthMonth = birthCalendar.get(Calendar.MONTH)
        val birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH)
        val nowMonth = nowCalendar.get(Calendar.MONTH)
        val nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH)
        
        if (nowMonth < birthMonth || (nowMonth == birthMonth && nowDay < birthDay)) {
            years--
        }
        
        // Calculate days since last birthday
        val lastBirthday = Calendar.getInstance().apply {
            timeInMillis = birthdate
            set(Calendar.YEAR, birthCalendar.get(Calendar.YEAR) + years)
        }
        
        val daysSinceBirthday = TimeUnit.MILLISECONDS.toDays(now - lastBirthday.timeInMillis).toInt()
        
        // Calculate time components
        val totalSeconds = (ageMillis / 1000) % 86400
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        
        return "💀 ${years}y ${daysSinceBirthday}d ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}"
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Memento Mori")
            .setContentText(calculateAge())
            .setSmallIcon(R.drawable.ic_skull)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
}
