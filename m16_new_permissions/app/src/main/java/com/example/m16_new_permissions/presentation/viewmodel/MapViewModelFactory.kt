package com.example.m16_new_permissions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.m16_new_permissions.data.service.LocationService

class MapViewModelFactory(
    private val locationService: LocationService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(locationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}