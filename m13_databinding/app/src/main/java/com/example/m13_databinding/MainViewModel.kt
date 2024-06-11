package com.example.m13_databinding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"

    // StateFlow для отслеживания текста поиска
    private val _searchText = MutableStateFlow("")

    // Public property for two-way binding
    var searchText: String
        get() = _searchText.value
        set(value) {
            _searchText.value = value
        }

    // StateFlow для отслеживания, активна ли операция поиска
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()


    // StateFlow для выдачи текущего результата поиска
    private val _stateFlow: MutableStateFlow<String> = MutableStateFlow("Empty")
    val stateFlow: StateFlow<String> = _stateFlow.asStateFlow()

    // Инициализация логирования изменений searchEnabled
    init {
        viewModelScope.launch(Dispatchers.IO) {
            _searchText.debounce(300) // задержка в 300 мс перед запуском поиска
                .collect { text ->
                    if (text.length >= 3) {
                        search()
                    } else {
                        _stateFlow.value = "Введите не менее 3 символов"
                    }
            }
        }
    }


    // Симуляция операции поиска
    fun search() {
        if (_searchText.value.length >= 3 && !_isSearching.value) {
            _isSearching.value = true
            // Симуляция задержки поиска или обработки
            // Здесь должна быть запущена реальная логика поиска
            // Пока мы будем симулировать результат
            _stateFlow.value = "Идет поиск по запросу '${_searchText.value}'..."
            Log.d(TAG, "Search started for: ${_searchText.value}")

            // Симуляция задержки и завершения поиска
            viewModelScope.launch {
                delay(2000)
                Log.d(TAG, "Search ended for: ${_searchText.value}")
                _stateFlow.value = "По запросу '${_searchText.value}' ничего не найдено"
                _isSearching.value = false
            }
        }
    }
}