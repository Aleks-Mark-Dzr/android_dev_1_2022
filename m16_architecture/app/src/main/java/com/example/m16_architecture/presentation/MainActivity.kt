package com.example.m16_architecture.presentation


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.m16_architecture.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetFact.setOnClickListener {
            viewModel.reloadFactsAboutNumbers()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.fact.collect { fact ->
                binding.tvFact.text = fact?.text ?: "No fact available"
            }
        }
    }
}