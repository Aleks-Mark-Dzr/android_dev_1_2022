package com.example.m11_timer_data_storage

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.m11_timer_data_storage.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var repository: Repository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = Repository(applicationContext)

        updateTextView()

        binding.saveButton.setOnClickListener {
            val text = binding.editText.text.toString() // Получаем текст из EditText
            repository.saveText(text) // Сохраняем текст через Repository
            updateTextView()
        }

        binding.clearButton.setOnClickListener {
            repository.clearText() // Вызываем метод очистки данных в Repository
            updateTextView()
        }
    }

    private fun updateTextView() {
        binding.textView.text = repository.getText()
    }
}