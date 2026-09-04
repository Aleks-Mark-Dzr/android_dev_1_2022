package com.example.m16_new_permissions.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.repository.AttractionRepository
import com.example.m16_new_permissions.domain.service.ILocationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(
    private val locationService: ILocationService,
    private val attractionRepository: AttractionRepository
) : ViewModel() {

    // Текущее местоположение
    val currentLocation: StateFlow<GeoPoint?> get() = locationService.currentLocation

    // Список достопримечательностей
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> get() = _attractions

    // Итог работы с резервной копией: показываем его один раз, поэтому не StateFlow
    private val _backupEvents = MutableSharedFlow<BackupEvent>()
    val backupEvents: SharedFlow<BackupEvent> get() = _backupEvents

    init {
        loadAttractions()
    }

    // Загрузка достопримечательностей из репозитория
    private fun loadAttractions() {
        viewModelScope.launch {
            // Загружаем достопримечательности из репозитория и сохраняем в переменную
            val loadedAttractions = attractionRepository.getAttractions()

            // Логируем загруженные данные для проверки
            Log.d("MapViewModel", "Loaded attractions: $loadedAttractions")

            // Обновляем значение StateFlow
            _attractions.value = loadedAttractions
        }
    }


    fun requestLocationPermission(fragment: androidx.fragment.app.Fragment) {
        locationService.requestLocationPermission(fragment)
    }

    fun updateCurrentLocation(geoPoint: GeoPoint) {
        locationService.updateLocation(geoPoint)
    }

    // Геопозиция, по которой можно поставить метку: сначала уже известная, затем последняя из системы
    fun resolveCurrentLocation(): GeoPoint? =
        locationService.currentLocation.value ?: locationService.getLastKnownLocation()

    // Добавление метки с описанием и фотографией в указанной точке
    fun addAttraction(
        name: String,
        description: String,
        geoPoint: GeoPoint,
        photoName: String? = null
    ) {
        viewModelScope.launch {
            val attraction = Attraction(
                name = name,
                description = description,
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                isUserAdded = true,
                photoName = photoName
            )
            attractionRepository.addAttraction(attraction)

            Log.d("MapViewModel", "Added attraction: $attraction")

            // Перечитываем список, чтобы карта обновилась
            _attractions.value = attractionRepository.getAttractions()
        }
    }

    // Редактирование сохранённой метки: название, описание, координаты и фотография.
    // Фото по умолчанию остаётся прежним — при перетаскивании метки его менять не нужно
    fun updateAttraction(
        attraction: Attraction,
        name: String,
        description: String,
        geoPoint: GeoPoint,
        photoName: String? = attraction.photoName
    ) {
        viewModelScope.launch {
            val updated = attraction.copy(
                name = name,
                description = description,
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                photoName = photoName,
                // Отметка времени нужна восстановлению из копии: по ней видно, чья версия свежее
                updatedAt = System.currentTimeMillis()
            )
            attractionRepository.updateAttraction(updated)

            Log.d("MapViewModel", "Updated attraction: $updated")

            // Перечитываем список, чтобы карта обновилась
            _attractions.value = attractionRepository.getAttractions()
        }
    }

    // Удаление сохранённой метки
    fun deleteAttraction(attraction: Attraction) {
        viewModelScope.launch {
            attractionRepository.deleteAttraction(attraction.id)

            Log.d("MapViewModel", "Deleted attraction: $attraction")

            _attractions.value = attractionRepository.getAttractions()
        }
    }

    /**
     * Подсказки поиска: метки, в названии или описании которых встречается набранный текст.
     * Совпадения с начала названия идут первыми — обычно ищут именно их, — а список
     * ограничиваем: длинный выпадающий перечень закрывает собой карту.
     */
    fun findAttractions(query: String): List<Attraction> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return emptyList()

        return _attractions.value
            .filter {
                it.name.contains(normalized, ignoreCase = true) ||
                        it.description.contains(normalized, ignoreCase = true)
            }
            .sortedWith(
                compareBy(
                    { !it.name.startsWith(normalized, ignoreCase = true) },
                    { it.name.lowercase() }
                )
            )
            .take(MAX_SUGGESTIONS)
    }

    // Выгрузка меток с фотографиями в файл, который пользователь выбрал системным диалогом
    fun exportBackup(target: Uri) {
        viewModelScope.launch {
            val event = attractionRepository.exportUserAttractions(target).fold(
                onSuccess = { BackupEvent.Exported(it) },
                onFailure = { BackupEvent.ExportFailed }
            )
            _backupEvents.emit(event)
        }
    }

    fun importBackup(source: Uri) {
        viewModelScope.launch {
            val event = attractionRepository.importUserAttractions(source).fold(
                onSuccess = { summary ->
                    // Восстановленные метки должны сразу появиться на карте
                    _attractions.value = attractionRepository.getAttractions()
                    BackupEvent.Imported(summary.added, summary.updated)
                },
                onFailure = { BackupEvent.ImportFailed }
            )
            _backupEvents.emit(event)
        }
    }

    private companion object {
        // Сколько подсказок показываем в выпадающем списке поиска
        const val MAX_SUGGESTIONS = 10
    }

    /** Что случилось с резервной копией: текст сообщения подбирает экран */
    sealed interface BackupEvent {
        data class Exported(val count: Int) : BackupEvent
        data class Imported(val added: Int, val updated: Int) : BackupEvent
        object ExportFailed : BackupEvent
        object ImportFailed : BackupEvent
    }
}
