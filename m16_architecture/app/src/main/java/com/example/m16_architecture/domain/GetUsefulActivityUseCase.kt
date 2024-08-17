package com.example.m16_architecture.domain

import com.example.m16_architecture.data.FactsAboutNumbersRepository
import com.example.m16_architecture.entity.FactsAboutNumbers
import javax.inject.Inject

class GetFactsAboutNumbersUseCase @Inject constructor(
    private val repository: FactsAboutNumbersRepository
) {
    suspend fun execute(): FactsAboutNumbers? {
        return repository.getFactsAboutNumbers()
    }
}