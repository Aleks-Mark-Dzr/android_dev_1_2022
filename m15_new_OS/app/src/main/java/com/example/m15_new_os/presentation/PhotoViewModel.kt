package com.example.m15_new_os.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.m15_new_os.data.AppDatabase
import com.example.m15_new_os.data.PhotoRepository
import com.example.m15_new_os.models.PhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PhotoRepository
    val allPhotos: StateFlow<List<PhotoEntity>>

    init {
        val photoDao = AppDatabase.getDatabase(application).photoDao()
        repository = PhotoRepository(photoDao)
        allPhotos = repository.allPhotos
        Log.d("PhotoViewModel", "PhotoDao initialized")
    }

    fun insert(photo: PhotoEntity) = viewModelScope.launch {
        try {
            repository.insert(photo)
            Log.d("PhotoViewModel", "Photo inserted: ${photo.photoUri}")
        } catch (e: Exception) {
            Log.e("PhotoViewModel", "Failed to insert photo", e)
        }
    }
}