package com.example.m17_new_firebase.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.m17_new_firebase.domain.repository.AttractionRepository

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