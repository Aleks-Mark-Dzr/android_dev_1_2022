package com.example.passengercounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.passengercounter.databinding.ActivityMainBinding

private const val MAX_SEATS = 49

class MainActivity : AppCompatActivity() {
    private var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCondition(binding)

        binding.buttonPlus.setOnClickListener {
            checkStatus(binding)
            counter++
            checkStatus(binding)
        }

        binding.buttonMinus.setOnClickListener {
            checkStatus(binding)
            counter--
            checkStatus(binding)
        }

        binding.buttonReset.setOnClickListener {
            startCondition(binding)
        }
    }

    private fun checkStatus(binding: ActivityMainBinding) {
        when (counter) {
            0 -> startCondition(binding)
            in 1..MAX_SEATS -> workCondition(binding)
            MAX_SEATS + 1 -> finishCondition(binding)
        }
    }

    private fun startCondition(binding: ActivityMainBinding) {
        counter = 0
        binding.buttonMinus.isEnabled = false
        binding.buttonPlus.isEnabled = true
        binding.buttonReset.visibility = View.INVISIBLE
        binding.textViewStatus.text = getText(R.string.seats_free)
        binding.textViewStatus.setTextColor(getColor(R.color.green))
        binding.textViewPassengerCounter.text = counter.toString()
    }

    private fun workCondition(binding: ActivityMainBinding) {
        binding.buttonMinus.isEnabled = true
        binding.buttonPlus.isEnabled = true
        binding.buttonReset.visibility = View.INVISIBLE
        binding.textViewStatus.setTextColor(getColor(R.color.purple))
        binding.textViewPassengerCounter.text = counter.toString()
        val outputText = "${getText(R.string.seats_left)} ${MAX_SEATS - counter}"
        binding.textViewStatus.text = outputText
    }

    private fun finishCondition(binding: ActivityMainBinding) {
        binding.buttonMinus.isEnabled = false
        binding.buttonPlus.isEnabled = false
        binding.buttonReset.visibility = View.VISIBLE
        binding.textViewStatus.text = getText(R.string.no_seats)
        binding.textViewStatus.setTextColor(getColor(R.color.red))
        binding.textViewPassengerCounter.text = counter.toString()
    }
}