package com.example.m16_new_permissions.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.m16_new_permissions.data.service.LocationService
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class MapViewModel(private val locationService: LocationService) : ViewModel() {

    val currentLocation: StateFlow<GeoPoint?> get() = locationService.currentLocation

    fun requestLocationPermission(fragment: androidx.fragment.app.Fragment) {
        locationService.requestLocationPermission(fragment)
    }

    fun updateCurrentLocation(geoPoint: GeoPoint) {
        locationService.updateLocation(geoPoint)
    }
}