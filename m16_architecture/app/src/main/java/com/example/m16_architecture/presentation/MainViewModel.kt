package com.example.m16_architecture.presentation


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m16_architecture.domain.GetFactsAboutNumbersUseCase
import com.example.m16_architecture.entity.FactsAboutNumbers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getFactsAboutNumbersUseCase: GetFactsAboutNumbersUseCase
) : ViewModel() {

    private val _fact = MutableStateFlow<FactsAboutNumbers?>(null)
    val fact: StateFlow<FactsAboutNumbers?> = _fact

    fun reloadFactsAboutNumbers() {
        viewModelScope.launch {
            try {
                val newFact = getFactsAboutNumbersUseCase.execute()
                if (newFact != null) {
                    _fact.value = newFact
                } else {
                    Log.e("MainViewModel", "No fact found")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading fact", e)
            }
        }
    }
}