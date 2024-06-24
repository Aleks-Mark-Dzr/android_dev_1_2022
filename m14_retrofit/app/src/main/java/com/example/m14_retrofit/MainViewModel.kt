package com.example.m14_retrofit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class UserData(
    val fullName: String,
    val email: String,
    val phone: String,
    val cell: String,
    val location: String,
    val pictureUrl: String
)

class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> = _userData

    fun fetchRandomUser() {
        viewModelScope.launch {
            Log.d(TAG, "Запрос к API для получения случайного пользователя")

            try {
                val response = RetrofitService.searchRandomuserApi.getRandomUser()

                if (response.isSuccessful) {
                    val user = response.body()?.results?.first()

                    if (user != null) {
                        val fullName = "${user.name.title} ${user.name.first} ${user.name.last}"
                        val location = "${user.location.city}, ${user.location.state}, ${user.location.country}"
                        val userData = UserData(
                            fullName = fullName,
                            email = user.email,
                            phone = user.phone,
                            cell = user.cell,
                            location = location,
                            pictureUrl = user.picture.large
                        )
                        _userData.postValue(userData)
                        Log.d(TAG, "Полученные данные пользователя: $fullName")
                    } else {
                        Log.e(TAG, "Пользователь не найден в ответе")
                        _userData.postValue(null)
                    }
                } else {
                    Log.e(TAG, "Не удалось получить данные пользователя: ${response.errorBody()?.string()}")
                    _userData.postValue(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении данных пользователя", e)
                _userData.postValue(null)
            }
        }
    }
}

