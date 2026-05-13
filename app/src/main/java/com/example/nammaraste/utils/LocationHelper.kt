package com.example.nammaraste.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class LatLng(val latitude: Double, val longitude: Double)

class LocationHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Checks if GPS or Network provider is enabled.
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Fetches the current GPS location using FusedLocationProviderClient.
     * Robust implementation with fallback and timeout.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLng {
        if (!isLocationEnabled()) {
            return LatLng(0.0, 0.0)
        }

        return try {
            // 1. Try to get last known location first (fastest)
            val lastLocation = fusedClient.lastLocation.await()
            if (lastLocation != null && (System.currentTimeMillis() - lastLocation.time) < 60000) {
                return LatLng(lastLocation.latitude, lastLocation.longitude)
            }

            // 2. Try to get fresh location with high accuracy
            val cts = CancellationTokenSource()
            val freshLocation = withTimeoutOrNull(10000) {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            }

            if (freshLocation != null) {
                LatLng(freshLocation.latitude, freshLocation.longitude)
            } else {
                // 3. Last fallback if high accuracy timed out or failed
                if (lastLocation != null) {
                    LatLng(lastLocation.latitude, lastLocation.longitude)
                } else {
                    LatLng(0.0, 0.0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LatLng(0.0, 0.0)
        }
    }
}