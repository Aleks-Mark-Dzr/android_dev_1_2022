package com.example.m17_new_firebase.domain.service

import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

interface ILocationService {
    val currentLocation: StateFlow<GeoPoint?>
    fun requestLocationPermission(fragment: Fragment)
    fun updateLocation(geoPoint: GeoPoint)
}