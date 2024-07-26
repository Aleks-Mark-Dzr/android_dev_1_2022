package com.example.m15_room


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.m15_roomtest.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val wordViewModel: WordViewModel by viewModels {
        WordViewModelFactory((application as WordsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addInputWordButton.setOnClickListener {
            val word = binding.textInput.text.toString()
            if (isValidWord(word)) {
                lifecycleScope.launch {
                    wordViewModel.insertOrUpdateWord(word)
                    binding.topWords.text = word
                }
                binding.textInput.text.clear()
            } else {
                showSnackbar("Пожалуйста, введите только буквы и дефис.")
            }
        }

        binding.deleteAllButton.setOnClickListener {
            lifecycleScope.launch {
                wordViewModel.deleteAllWords()
            }
        }

        wordViewModel.topWords.observe(this) { words ->
            val topWordsText = words.joinToString(separator = "\n") { "${it.word}: ${it.count}" }
            binding.topWords.text = topWordsText
        }
    }

    private fun isValidWord(word: String): Boolean {
        val regex = "^[a-zA-Z-]+$".toRegex()
        return word.isNotBlank() && word.matches(regex)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}