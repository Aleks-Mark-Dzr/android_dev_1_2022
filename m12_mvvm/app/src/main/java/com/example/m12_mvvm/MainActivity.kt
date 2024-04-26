package com.example.m12_mvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.m12_mvvm.databinding.FragmentMainBinding
import com.example.m12_mvvm.ui.theme.M12_mvvmTheme

private lateinit var binding: FragmentMainBinding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}