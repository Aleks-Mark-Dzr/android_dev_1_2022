package com.example.m14_retrofit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
//    private fun fetchRandomUser() {
//        lifecycleScope.launch {
//            try {
//                val response = RetrofitService.searchRandomuserApi.getRandomUser()
//                val user = response.results.firstOrNull()
//
//                user?.let {
//                    val imageUrl = it.picture.large
//                    Glide.with(this@MainFragment)
//                        .load(imageUrl)
//                        .placeholder(R.drawable.ic_placeholder)
//                        .into(binding.imageViewProfile)
//
//                    binding.textViewName.text = "${it.name.first} ${it.name.last}"
//                    binding.textViewEmail.text = it.email
//                    binding.textViewPhone.text = it.phone
//                    binding.textViewCell.text = it.cell
//                    binding.textViewLocation.text = "${it.location.city}, ${it.location.country}"
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Обработка ошибок
//            }
//        }
//    }
}