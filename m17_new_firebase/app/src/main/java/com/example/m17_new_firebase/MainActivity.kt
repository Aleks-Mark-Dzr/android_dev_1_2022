package com.example.m17_new_firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m17_new_firebase.databinding.ActivityMainBinding
import com.example.m17_new_firebase.ui.map.MapFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Добавляем MapFragment в MainActivity
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment())
                .commit()
        }
    }
}