package com.example.m10_timer_life_cycle

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.m10_timer_life_cycle.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hasMiddleName = intent.getBooleanExtra(PersonContract.EXTRA_HAS_MIDDLE_NAME, false)
        binding.middleNameLayout.isVisible = hasMiddleName

        binding.saveButton.setOnClickListener {
            val surname = binding.surname.text.toString()
            val name = binding.name.text.toString()
            val middleName = if (hasMiddleName) binding.middleName.text.toString() else null
            val person = Person(surname, name, middleName)

            val resultIntent = Intent()
            resultIntent.putExtra(PersonContract.EXTRA_PERSON, person)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        Log.d("SecondActivityTest", "onCreate")
    }

    companion object {
        const val EXTRA_HAS_MIDDLE_NAME: String = "com.example.hasMiddleName"
    }

    override fun onStart() {
        super.onStart()
        Log.d("SecondActivityTest", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("SecondActivityTest", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("SecondActivityTest", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("SecondActivityTest", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SecondActivityTest", "onDestroy")
    }
}