package com.example.m12_mvvm

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
    // StateFlow для отслеживания текста поиска
    private val _searchText = MutableStateFlow("")

    // StateFlow для отслеживания, активна ли операция поиска
    private val _isSearching = MutableStateFlow(false)

    // Объединяем текст и состояние поиска для активации кнопки поиска
    val searchEnabled: StateFlow<Boolean> = combine(_searchText, _isSearching) { text, isSearching ->
        text.length >= 3 && !isSearching
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default), // Используйте ViewModelScope если доступно
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    // StateFlow для выдачи текущего результата поиска
    private val _stateFlow: MutableStateFlow<String> = MutableStateFlow("Empty")
    val stateFlow: StateFlow<String> = _stateFlow.asStateFlow()

    // Функция для установки текущего текста поиска из UI
    fun setSearchText(text: String) {
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

            // Симуляция задержки и завершения поиска
            viewModelScope.launch {
                delay(2000)
                _stateFlow.value = "Результаты для '${_searchText.value}'"
                _isSearching.value = false
            }
        }
    }
}