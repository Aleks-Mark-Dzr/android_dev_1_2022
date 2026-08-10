package com.example.screenlayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.screenlayout.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.customView.setFirstText(getText(R.string.first_text).toString())
        binding.customView.setSecondText(getText(R.string.second_text).toString())
    }
}