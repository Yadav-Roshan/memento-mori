package com.mementomori

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("memento_mori", Context.MODE_PRIVATE)
            val birthdate = prefs.getLong("birthdate", 0L)
            
            if (birthdate > 0) {
                val serviceIntent = Intent(context, AgeService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}
