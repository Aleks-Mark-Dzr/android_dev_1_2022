package com.example.m10_timer_life_cycle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.m10_timer_life_cycle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedInstanceState?.getString("SAVED_PERSON_DATA")?.let {
            binding.person.apply {
                text = it
                visibility = View.VISIBLE
            }
        }

        val resultLauncher = registerForActivityResult(PersonContract()) { person ->
            person?.let {
                val fullText = "${it.surName} ${it.name} ${it.middleName.orEmpty()}"
                binding.person.apply {
                    text = fullText
                    visibility = View.VISIBLE
                }
            }
        }

        binding.introduceYourselfButton.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra(PersonContract.EXTRA_HAS_MIDDLE_NAME, binding.checkbox.isChecked)
            if (binding.checkbox.isChecked){
                resultLauncher.launch(true)
            } else {
                resultLauncher.launch(false)
            }
        }
        Log.d("MainActivityTest", "onCreate")
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SAVED_PERSON_DATA", binding.person.text.toString())
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivityTest", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivityTest", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivityTest", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivityTest", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivityTest", "onDestroy")
    }
}