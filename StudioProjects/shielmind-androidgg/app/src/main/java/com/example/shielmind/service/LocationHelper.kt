package com.example.shielmind.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

object LocationHelper {
    private const val TAG = "LocationHelper"

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            var location: Location? = null

            if (isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (isGpsEnabled && location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
            return location
        } catch (e: SecurityException) {
            Log.e(TAG, "Permissions manquantes pour la géolocalisation : ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de géolocalisation : ${e.message}")
        }
        return null
    }

    /**
     * Tries to request a single fresh location update if last known is null.
     */
    @SuppressLint("MissingPermission")
    fun requestFreshLocationUpdate(context: Context, onLocationResult: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            val provider = when {
                isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
                isGpsEnabled -> LocationManager.GPS_PROVIDER
                else -> null
            }

            if (provider == null) {
                onLocationResult(getCurrentLocation(context))
                return
            }

            // Standard callback-based request
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    onLocationResult(location)
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, context.mainLooper)

            // As a safeguard, if no immediate update, return the last known
            val current = getCurrentLocation(context)
            if (current != null) {
                onLocationResult(current)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permissions manquantes pour la géolocalisation fraîche : ${e.message}")
            onLocationResult(null)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de géolocalisation fraîche : ${e.message}")
            onLocationResult(null)
        }
    }
}
