package com.example.m15_new_os.data

import com.example.m15_new_os.dao.PhotoDao
import com.example.m15_new_os.models.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PhotoRepository(private val photoDao: PhotoDao) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allPhotos: StateFlow<List<PhotoEntity>> = photoDao.getAllPhotos()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun insert(photo: PhotoEntity) {
        photoDao.insert(photo)
    }
}
