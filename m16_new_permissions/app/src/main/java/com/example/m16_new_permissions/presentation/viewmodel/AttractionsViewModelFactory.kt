package com.example.m16_new_permissions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.m16_new_permissions.domain.repository.AttractionRepository

class AttractionsViewModelFactory(
    private val repository: AttractionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttractionsViewModel::class.java)) {
            return AttractionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}