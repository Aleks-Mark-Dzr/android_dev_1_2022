package com.example.m16_new_permissions.domain.repository

//import com.example.app.domain.model.Attraction
import com.example.m16_new_permissions.domain.model.Attraction

interface AttractionRepository {
    suspend fun getAttractions(): List<Attraction>
}