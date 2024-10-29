package com.example.m16_new_permissions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m16_new_permissions.data.repository.AttractionRepositoryImpl
import com.example.m16_new_permissions.domain.model.Attraction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttractionsViewModel(
    private val repository: AttractionRepositoryImpl
) : ViewModel() {

    // Поток, который содержит список достопримечательностей
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> get() = _attractions

    // Функция для загрузки достопримечательностей по заданным координатам
    fun loadAttractions(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                // Загружаем список достопримечательностей из репозитория
                _attractions.value = repository.getAttractions(latitude, longitude)
            } catch (e: Exception) {
                // Обработка ошибок: лог или оповещение пользователя о проблемах с загрузкой данных
                _attractions.value = emptyList()
            }
        }
    }
}