package com.example.m12_mvvm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"

    // StateFlow для отслеживания текста поиска
    private val _searchText = MutableStateFlow("")

    // StateFlow для отслеживания, активна ли операция поиска
    private val _isSearching = MutableStateFlow(false)

    // Объединяем текст и состояние поиска для активации кнопки поиска
    val searchEnabled: StateFlow<Boolean> = combine(_searchText, _isSearching) { text, isSearching ->
        text.length >= 3 && !isSearching
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    // StateFlow для выдачи текущего результата поиска
    private val _stateFlow: MutableStateFlow<String> = MutableStateFlow("Empty")
    val stateFlow: StateFlow<String> = _stateFlow.asStateFlow()

    // Инициализация логирования изменений searchEnabled
    init {
        viewModelScope.launch(Dispatchers.IO) {
            searchEnabled.collect { isEnabled ->
                Log.d(TAG, "Search enabled: $isEnabled")
            }
        }
    }


    // Функция для установки текущего текста поиска из UI
    fun setSearchText(text: String) {
        Log.d(TAG, "Setting search text: $text")
        _searchText.value = text
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
                _stateFlow.value = "Результаты для '${_searchText.value}'"
                _isSearching.value = false
            }
        }
    }
}