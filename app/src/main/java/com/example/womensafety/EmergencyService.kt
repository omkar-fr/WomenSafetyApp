package com.example.womensafety

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Looper

class EmergencyService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var smsManager: SmsManager
    private var currentLocation: Location? = null
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "emergency_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        smsManager = SmsManager.getDefault()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        requestLocationUpdates()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Emergency Channel"
            val descriptionText = "Channel for emergency notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Emergency Alert Active")
            .setContentText("Your emergency contacts are being notified with your location")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationUpdates() {
        try {
            // Try to get last known location first
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                currentLocation = lastKnownLocation
                sendEmergencyMessages(lastKnownLocation)
            }

            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // 5 seconds
                10f,  // 10 meters
                this,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmergencyMessages(location: Location) {
        val sharedPreferences = getSharedPreferences("WomenSafetyApp", Context.MODE_PRIVATE)
        val contact1 = sharedPreferences.getString("emergencyContact1", "") ?: ""
        val contact2 = sharedPreferences.getString("emergencyContact2", "") ?: ""

        val message = "EMERGENCY: I need help! My current location is: " +
                "https://maps.google.com/?q=${location.latitude},${location.longitude}"

        try {
            if (contact1.isNotBlank()) {
                smsManager.sendTextMessage(contact1, null, message, null, null)
            }
            if (contact2.isNotBlank()) {
                smsManager.sendTextMessage(contact2, null, message, null, null)
            }
            Toast.makeText(this, "Emergency messages sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        // Only send updated messages if location has changed significantly
        sendEmergencyMessages(location)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.removeUpdates(this)
        } catch (e: SecurityException) {
            // Ignore
        }
    }
}