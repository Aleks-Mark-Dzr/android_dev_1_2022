package com.example.m13_new_list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.m13_new_list.databinding.ActivityMainBinding
import com.example.m13_new_list.photoslist.MarsPhotosAdapter
import com.example.m13_new_list.photoslist.MarsPhotosViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MarsPhotosViewModel
    private lateinit var adapter: MarsPhotosAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Используем ViewBinding для связывания с макетом
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this).get(MarsPhotosViewModel::class.java)

        // Настраиваем RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Наблюдаем за изменениями в StateFlow с использованием lifecycleScope
        lifecycleScope.launch {
            viewModel.photos.collect { photos ->
                adapter = MarsPhotosAdapter(photos) { photo ->
                    // Открытие фото на отдельном экране
                }
                binding.recyclerView.adapter = adapter
            }
        }

        // Загружаем фотографии с Марса
        viewModel.fetchMarsPhotos(1000, "UodhjJsYYRi9DCzJcxEDLCL2Ue7EwhVBQZufhHxf")
    }
}