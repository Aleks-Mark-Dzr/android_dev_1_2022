package com.example.m13_new_list.photoslist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m13_new_list.api.RetrofitInstance
import com.example.m13_new_list.models.Photo
import kotlinx.coroutines.launch

class MarsPhotosViewModel : ViewModel() {
    val photos = MutableLiveData<List<Photo>>()

    fun fetchMarsPhotos(sol: Int, apiKey: String) {
        viewModelScope.launch {
            val response = RetrofitInstance.api.getMarsPhotos(sol, apiKey)
            photos.postValue(response.photos)
        }
    }
}