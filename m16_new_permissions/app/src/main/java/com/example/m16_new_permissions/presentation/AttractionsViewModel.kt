package com.example.m16_new_permissions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.repository.AttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttractionsViewModel(private val repository: AttractionRepository) : ViewModel() {

    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> get() = _attractions

    init {
        loadAttractions()
    }

    private fun loadAttractions() {
        viewModelScope.launch {
            try {
                _attractions.value = repository.getAttractions()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}