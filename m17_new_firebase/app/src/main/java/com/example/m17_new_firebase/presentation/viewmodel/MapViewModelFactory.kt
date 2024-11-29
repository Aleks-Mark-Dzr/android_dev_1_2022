package com.example.m17_new_firebase.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.m17_new_firebase.domain.repository.AttractionRepository
import com.example.m17_new_firebase.domain.service.ILocationService

class MapViewModelFactory(
    private val locationService: ILocationService,
    private val attractionRepository: AttractionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(locationService, attractionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}