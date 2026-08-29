package com.example.m16_new_permissions.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.m16_new_permissions.domain.service.ILocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class LocationService(private val context: Context) : ILocationService {

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    override val currentLocation: StateFlow<GeoPoint?> get() = _currentLocation

    override fun requestLocationPermission(fragment: androidx.fragment.app.Fragment) {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(fragment.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }

    override fun updateLocation(geoPoint: GeoPoint) {
        _currentLocation.value = geoPoint
    }

    override fun getLastKnownLocation(): GeoPoint? {
        if (!hasLocationPermission()) return null

        val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

        // Берём самые свежие координаты из доступных провайдеров
        val location = providers
            .mapNotNull { provider ->
                try {
                    locationManager.getLastKnownLocation(provider)
                } catch (e: SecurityException) {
                    null
                } catch (e: IllegalArgumentException) {
                    // Провайдер отсутствует на устройстве
                    null
                }
            }
            .maxByOrNull { it.time }
            ?: return null

        return GeoPoint(location.latitude, location.longitude).also { _currentLocation.value = it }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
