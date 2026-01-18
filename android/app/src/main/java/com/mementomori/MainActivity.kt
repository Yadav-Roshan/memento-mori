package com.mementomori

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker
    private lateinit var saveButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    
    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        datePicker = findViewById(R.id.datePicker)
        timePicker = findViewById(R.id.timePicker)
        saveButton = findViewById(R.id.saveButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        
        timePicker.setIs24HourView(true)
        
        loadSavedBirthdate()
        
        saveButton.setOnClickListener {
            saveBirthdate()
        }
        
        startButton.setOnClickListener {
            requestNotificationPermissionAndStart()
        }
        
        stopButton.setOnClickListener {
            stopAgeService()
        }
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
    
    private fun loadSavedBirthdate() {
        val prefs = getSharedPreferences("memento_mori", MODE_PRIVATE)
        val birthdate = prefs.getLong("birthdate", 0L)
        
        if (birthdate > 0) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = birthdate
            
            datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(Calendar.MINUTE)
        }
    }
    
    private fun saveBirthdate() {
        val calendar = Calendar.getInstance()
        calendar.set(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth,
            timePicker.hour,
            timePicker.minute,
            0
        )
        
        val prefs = getSharedPreferences("memento_mori", MODE_PRIVATE)
        prefs.edit().putLong("birthdate", calendar.timeInMillis).apply()
        
        Toast.makeText(this, "Birthdate saved!", Toast.LENGTH_SHORT).show()
        
        // Restart service if running
        if (AgeService.isRunning) {
            stopAgeService()
            startAgeService()
        }
    }
    
    private fun requestNotificationPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
                return
            }
        }
        startAgeService()
    }
    
    private fun startAgeService() {
        val prefs = getSharedPreferences("memento_mori", MODE_PRIVATE)
        if (prefs.getLong("birthdate", 0L) == 0L) {
            Toast.makeText(this, "Please set your birthdate first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, AgeService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Memento Mori started!", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopAgeService() {
        val intent = Intent(this, AgeService::class.java)
        stopService(intent)
        Toast.makeText(this, "Memento Mori stopped", Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAgeService()
            } else {
                Toast.makeText(this, "Notification permission required!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
