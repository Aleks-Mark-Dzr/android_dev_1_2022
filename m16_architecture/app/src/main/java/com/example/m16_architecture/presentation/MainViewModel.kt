package com.example.m16_architecture.presentation


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
            _fact.value = getFactsAboutNumbersUseCase.execute()
        }
    }
}