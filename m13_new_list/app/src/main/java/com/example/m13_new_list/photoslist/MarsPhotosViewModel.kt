package com.example.m13_new_list.photoslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m13_new_list.api.MarsRawApi
import com.example.m13_new_list.api.RetrofitInstance
import com.example.m13_new_list.models.Photo
import com.example.m13_new_list.models.PhotoSource
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

    // Выбранный источник переживает поворот экрана вместе с ViewModel
    private val _source = MutableStateFlow(PhotoSource.PROCESSED)
    val source: StateFlow<PhotoSource> = _source

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun selectSource(source: PhotoSource) {
        if (_source.value == source) return
        _source.value = source
        _photos.value = emptyList()
    }

    /**
     * @param request сол для [PhotoSource.RAW] и поисковая строка для [PhotoSource.PROCESSED].
     */
    fun fetchMarsPhotos(request: String, page: Int = 1) {
        val source = _source.value
        val query = request.trim().ifBlank { source.defaultRequest }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val photos = when (source) {
                    PhotoSource.RAW -> loadRawPhotos(query, page)
                    PhotoSource.PROCESSED -> loadProcessedPhotos(query, page)
                }
                _photos.value = photos
                _errorMessage.value = if (photos.isEmpty()) emptyResultMessage(source, query) else null
            } catch (exception: NumberFormatException) {
                _photos.value = emptyList()
                _errorMessage.value = "Сол задаётся числом, например 1000"
            } catch (exception: HttpException) {
                _photos.value = emptyList()
                _errorMessage.value = "Ошибка сервера NASA: ${exception.code()}"
            } catch (exception: IOException) {
                _photos.value = emptyList()
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (exception: Exception) {
                _photos.value = emptyList()
                _errorMessage.value = "Не удалось загрузить фотографии"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadRawPhotos(request: String, page: Int): List<Photo> {
        val sol = request.toInt()
        val response = RetrofitInstance.rawApi.getPhotosBySol(
            solFrom = MarsRawApi.solCondition(sol, "gte"),
            solTo = MarsRawApi.solCondition(sol, "lte"),
            // у сырых кадров страницы считаются с нуля, в поиске — с единицы
            page = (page - 1).coerceAtLeast(0)
        )
        return response.items.mapNotNull { it.toPhoto() }
    }

    private suspend fun loadProcessedPhotos(request: String, page: Int): List<Photo> {
        val response = RetrofitInstance.imagesApi.searchPhotos(query = request, page = page)
        return response.collection?.items.orEmpty().mapNotNull { it.toPhoto() }
    }

    private fun emptyResultMessage(source: PhotoSource, query: String) = when (source) {
        PhotoSource.RAW -> "За сол $query снимков нет"
        PhotoSource.PROCESSED -> "По запросу «$query» ничего не найдено"
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
