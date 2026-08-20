package com.example.m13_new_list

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.m13_new_list.databinding.ActivityMainBinding
import com.example.m13_new_list.models.PhotoSource
import com.example.m13_new_list.photoslist.MarsPhotosAdapter
import com.example.m13_new_list.photoslist.MarsPhotosViewModel
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

        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (message != null) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        setUpSourceSwitch(isFirstStart = savedInstanceState == null)
        binding.showButton.setOnClickListener { loadPhotos() }
        binding.requestInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) loadPhotos()
            false
        }

        // Загружаем фотографии с Марса. После поворота список уже лежит во ViewModel
        if (savedInstanceState == null) {
            loadPhotos()
        }
    }

    private fun setUpSourceSwitch(isFirstStart: Boolean) {
        // Источник живёт во ViewModel, поэтому после поворота восстанавливаем разметку по нему
        applySource(viewModel.source.value, resetInput = isFirstStart)

        binding.sourceGroup.setOnCheckedChangeListener { _, checkedId ->
            val source =
                if (checkedId == R.id.rawButton) PhotoSource.RAW else PhotoSource.PROCESSED
            // Отсекаем срабатывание от программной установки галки в applySource
            if (source == viewModel.source.value) return@setOnCheckedChangeListener

            viewModel.selectSource(source)
            applySource(source, resetInput = true)
            loadPhotos()
        }
    }

    /** У источников разный смысл поля ввода: сол — число, поиск — текст. */
    private fun applySource(source: PhotoSource, resetInput: Boolean) {
        val checkedId = when (source) {
            PhotoSource.RAW -> R.id.rawButton
            PhotoSource.PROCESSED -> R.id.processedButton
        }
        if (binding.sourceGroup.checkedRadioButtonId != checkedId) {
            binding.sourceGroup.check(checkedId)
        }

        binding.requestInput.inputType = when (source) {
            PhotoSource.RAW -> InputType.TYPE_CLASS_NUMBER
            PhotoSource.PROCESSED -> InputType.TYPE_CLASS_TEXT
        }
        binding.requestInput.hint = getString(
            when (source) {
                PhotoSource.RAW -> R.string.hint_sol
                PhotoSource.PROCESSED -> R.string.hint_query
            }
        )
        if (resetInput) {
            binding.requestInput.setText(source.defaultRequest)
        }
    }

    private fun loadPhotos() {
        viewModel.fetchMarsPhotos(binding.requestInput.text.toString())
    }
}
