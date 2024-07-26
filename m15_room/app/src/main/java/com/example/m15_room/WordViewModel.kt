package com.example.m15_room

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WordViewModel(private val repository: WordRepository) : ViewModel() {

    val topWords: LiveData<List<Word>> = repository.topWords.asLiveData()

    fun insertOrUpdateWord(word: String) = viewModelScope.launch {
        val existingWord = repository.getWord(word)
        if (existingWord != null) {
            repository.update(existingWord.copy(count = existingWord.count + 1))
        } else {
            repository.insert(Word(word, 1))
        }
    }

    fun deleteAllWords() = viewModelScope.launch {
        repository.deleteAll()
    }
}

class WordViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}