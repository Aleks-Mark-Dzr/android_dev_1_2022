package com.example.skillbox_hw_quiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.example.skillbox_hw_quiz.databinding.ActivityWelcomeBinding
import com.example.skillbox_hw_quiz.databinding.MainActivityBinding
import com.example.skillbox_hw_quiz.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_welcome)

//        val navController = findNavController(R.id.nav_host_fragment)


//        binding.continueButton.setOnClickListener{
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, MainFragment())
//                .commit()
//            val navController = findNavController(R.id.fragment_container_view_tag)
//            navController.navigate(R.id.action_welcomeFragment_to_mainFragment)

//        }
    }
}