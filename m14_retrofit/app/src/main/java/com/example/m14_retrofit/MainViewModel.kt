////package com.example.m14_retrofit
////
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.lifecycleScope
////import com.bumptech.glide.Glide
////import kotlinx.coroutines.launch
////
////class MainViewModel: ViewModel() {
////
////
////
////    private fun fetchRandomUser() {
//////        lifecycleScope.launch {
//////            try {
//////                val response = RetrofitService.searchRandomuserApi.getRandomUser()
//////                val user = response.results.firstOrNull()
//////
//////                user?.let {
//////                    val imageUrl = it.picture.large
//////                    Glide.with(this@MainFragment)
//////                        .load(imageUrl)
//////                        .placeholder(R.drawable.ic_placeholder)
//////                        .into(binding.imageViewProfile)
//////
//////                    binding.textViewName.text = "${it.name.first} ${it.name.last}"
//////                    binding.textViewEmail.text = it.email
//////                    binding.textViewPhone.text = it.phone
//////                    binding.textViewCell.text = it.cell
//////                    binding.textViewLocation.text = "${it.location.city}, ${it.location.country}"
//////                }
//////            } catch (e: Exception) {
//////                e.printStackTrace()
//////                // Обработка ошибок
//////            }
////        }
////    }
////}
//
//package com.example.m14_retrofit
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.viewModelScope
//import com.bumptech.glide.Glide
//import com.example.m14_retrofit.databinding.FragmentMainBinding
//import kotlinx.coroutines.launch
//import retrofit2.Response
//
//
//class MainViewModel : ViewModel() {
//
//    private val TAG = "MainViewModel"
//    private lateinit var binding: FragmentMainBinding
//
//    // LiveData или StateFlow для хранения данных пользователя
//    private val _user = MutableLiveData<User?>()
//    val user: LiveData<User?> = _user
//
//    fun fetchRandomUser() {
//        viewModelScope.launch {
//
//            // Дополнительное логирование
//            Log.d(TAG, "Запрос к API для получения случайного пользователя")
//
//            try {
//                val response = RetrofitService.searchRandomuserApi.getRandomUser()
//
//                if (response.isSuccessful) {
//                    val user = response.body()?.results?.first()
//
//                    if (user != null) {
//                        val fullName = "${user.name.title} ${user.name.first} ${user.name.last}"
//                        Log.d(TAG, "Полученные данные пользователя: $fullName")
//
//                        binding.textViewName.text = fullName
//                        binding.textViewEmail.text = user.email
//                        binding.textViewPhone.text = user.phone
//                        binding.textViewCell.text = user.cell
//                        val location =
//                            "${user.location.city}, ${user.location.state}, ${user.location.country}"
//                        binding.textViewLocation.text = location
////                        Glide.with(this@MainViewModel).load(user.picture.large)
////                            .into(binding.imageViewProfile)
//                    } else {
//                        Log.e(TAG, "Пользователь не найден в ответе")
//                    }
//                } else {
//                    Log.e(
//                        TAG,
//                        "Не удалось получить данные пользователя: ${response.errorBody()?.string()}"
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Ошибка при получении данных пользователя", e)
//            }
//        }
//    }
//}

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

