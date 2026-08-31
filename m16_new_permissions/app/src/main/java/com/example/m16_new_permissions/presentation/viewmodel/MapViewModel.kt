package com.example.m16_new_permissions.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.repository.AttractionRepository
import com.example.m16_new_permissions.domain.service.ILocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(
    private val locationService: ILocationService,
    private val attractionRepository: AttractionRepository
) : ViewModel() {

    // Текущее местоположение
    val currentLocation: StateFlow<GeoPoint?> get() = locationService.currentLocation

    // Список достопримечательностей
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> get() = _attractions

    init {
        loadAttractions()
    }

    // Загрузка достопримечательностей из репозитория
    private fun loadAttractions() {
        viewModelScope.launch {
            // Загружаем достопримечательности из репозитория и сохраняем в переменную
            val loadedAttractions = attractionRepository.getAttractions()

            // Логируем загруженные данные для проверки
            Log.d("MapViewModel", "Loaded attractions: $loadedAttractions")

            // Обновляем значение StateFlow
            _attractions.value = loadedAttractions
        }
    }


    fun requestLocationPermission(fragment: androidx.fragment.app.Fragment) {
        locationService.requestLocationPermission(fragment)
    }

    fun updateCurrentLocation(geoPoint: GeoPoint) {
        locationService.updateLocation(geoPoint)
    }

    // Геопозиция, по которой можно поставить метку: сначала уже известная, затем последняя из системы
    fun resolveCurrentLocation(): GeoPoint? =
        locationService.currentLocation.value ?: locationService.getLastKnownLocation()

    // Добавление метки с описанием в указанной точке
    fun addAttraction(name: String, description: String, geoPoint: GeoPoint) {
        viewModelScope.launch {
            val attraction = Attraction(
                name = name,
                description = description,
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                isUserAdded = true
            )
            attractionRepository.addAttraction(attraction)

            Log.d("MapViewModel", "Added attraction: $attraction")

            // Перечитываем список, чтобы карта обновилась
            _attractions.value = attractionRepository.getAttractions()
        }
    }

    // Редактирование сохранённой метки: название, описание и координаты
    fun updateAttraction(
        attraction: Attraction,
        name: String,
        description: String,
        geoPoint: GeoPoint
    ) {
        viewModelScope.launch {
            val updated = attraction.copy(
                name = name,
                description = description,
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude
            )
            attractionRepository.updateAttraction(updated)

            Log.d("MapViewModel", "Updated attraction: $updated")

            // Перечитываем список, чтобы карта обновилась
            _attractions.value = attractionRepository.getAttractions()
        }
    }

    // Удаление сохранённой метки
    fun deleteAttraction(attraction: Attraction) {
        viewModelScope.launch {
            attractionRepository.deleteAttraction(attraction.id)

            Log.d("MapViewModel", "Deleted attraction: $attraction")

            _attractions.value = attractionRepository.getAttractions()
        }
    }
}
