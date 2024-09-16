package com.example.m13_new_list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.m13_new_list.databinding.ItemDetailFotoBinding
//import com.example.m13_new_list.databinding.ItemDetailFotoBinding

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ItemDetailFotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Используем ViewBinding для связывания с макетом
        binding = ItemDetailFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем переданные данные о фото
        val photoUrl = intent.getStringExtra("photo_url")
        val roverName = intent.getStringExtra("rover_name")
        val sol = intent.getIntExtra("sol", 0)
        val cameraName = intent.getStringExtra("camera_name")
        val earthDate = intent.getStringExtra("earth_date")

        // Загружаем фото с помощью Glide
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .into(binding.detailFotoView)
        }

        // Устанавливаем данные для текстовых полей
        binding.roverNameTextView.text = "Rover: $roverName"
        binding.solTextView.text = "Sol: $sol"
        binding.cameraNameTextView.text = "Camera: $cameraName"
        binding.dateTextView.text = "Date: $earthDate"
    }
}