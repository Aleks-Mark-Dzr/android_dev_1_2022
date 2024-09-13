package com.example.m13_new_list.photoslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m13_new_list.api.RetrofitInstance
import com.example.m13_new_list.models.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarsPhotosViewModel : ViewModel() {

    // Приватный MutableStateFlow
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    // Публичный неизменяемый StateFlow
    val photos: StateFlow<List<Photo>> = _photos

    fun fetchMarsPhotos(sol: Int, apiKey: String) {
        viewModelScope.launch {
            val response = RetrofitInstance.api.getMarsPhotos(sol, apiKey)
            _photos.value = response.photos
        }
    }
}