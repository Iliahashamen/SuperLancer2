package com.example.carggameapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class GameLocationManager(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLastLocation(callback: (Double, Double) -> Unit) {
        // 1. Check if we have permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // 2. Try to get the last known location (Fastest)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback(location.latitude, location.longitude)
                } else {
                    // 3. If "last known" is empty, force a new update
                    forceNewLocation(callback)
                }
            }
        } else {
            // No permission? Return 0.0, 0.0 (The middle of the ocean)
            callback(0.0, 0.0)
        }
    }

    private fun forceNewLocation(callback: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        callback(location.latitude, location.longitude)
                    } else {
                        callback(0.0, 0.0)
                    }
                }
        }
    }
}