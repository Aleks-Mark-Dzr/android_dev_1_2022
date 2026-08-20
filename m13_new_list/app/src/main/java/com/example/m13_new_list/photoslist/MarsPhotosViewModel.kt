package com.example.m13_new_list.photoslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m13_new_list.api.MarsApi
import com.example.m13_new_list.api.RetrofitInstance
import com.example.m13_new_list.models.Photo
import com.example.m13_new_list.models.toPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MarsPhotosViewModel : ViewModel() {

    // Приватный MutableStateFlow
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    // Публичный неизменяемый StateFlow
    val photos: StateFlow<List<Photo>> = _photos

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchMarsPhotos(sol: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMarsPhotos(
                    solFrom = MarsApi.solCondition(sol, "gte"),
                    solTo = MarsApi.solCondition(sol, "lte")
                )
                val photos = response.items
                    .map { it.toPhoto() }
                    .filter { it.img_src.isNotBlank() }
                _photos.value = photos
                _errorMessage.value =
                    if (photos.isEmpty()) "За сол $sol снимков нет" else null
            } catch (exception: HttpException) {
                _photos.value = emptyList()
                _errorMessage.value = "Ошибка сервера NASA: ${exception.code()}"
            } catch (exception: IOException) {
                _photos.value = emptyList()
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (exception: Exception) {
                _photos.value = emptyList()
                _errorMessage.value = "Не удалось загрузить фотографии"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
