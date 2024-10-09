package com.example.m15_new_os

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m15_new_os.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Используем View Binding для настройки макета
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}