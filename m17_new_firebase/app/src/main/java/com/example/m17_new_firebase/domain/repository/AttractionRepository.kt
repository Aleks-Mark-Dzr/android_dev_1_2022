package com.example.m17_new_firebase.domain.repository

//import com.example.app.domain.model.Attraction
import com.example.m17_new_firebase.domain.model.Attraction

interface AttractionRepository {
    suspend fun getAttractions(): List<Attraction>
}