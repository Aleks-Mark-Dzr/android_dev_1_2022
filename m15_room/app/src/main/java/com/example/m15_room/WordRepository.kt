package com.example.m15_room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WordRepository(private val wordDao: WordDao) {
    val topWords: Flow<List<Word>> = wordDao.getTopWords()

    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }

    suspend fun update(word: Word) {
        wordDao.update(word)
    }

    suspend fun deleteAll() {
        wordDao.deleteAll()
    }

    suspend fun getWord(word: String): Word? {
        return wordDao.getWord(word).firstOrNull()
    }
}