package com.example.m16_new_permissions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m16_new_permissions.databinding.ActivityMainBinding
import com.example.m16_new_permissions.ui.map.MapFragment

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