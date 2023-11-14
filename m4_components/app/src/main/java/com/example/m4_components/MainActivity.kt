package com.example.m4_components

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.example.m4_components.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nameText.doOnTextChanged { text, _, _, count -> checkAndEnableSaveButton(binding) }

        binding.phoneTextView.doOnTextChanged { text, _, _, _ -> checkAndEnableSaveButton(binding) }
        binding.maleRadioButton.setOnCheckedChangeListener { _, _ ->
            checkAndEnableSaveButton(
                binding
            )
        }
        binding.femaleRadioButton.setOnCheckedChangeListener { _, _ ->
            checkAndEnableSaveButton(
                binding
            )
        }
        binding.reciveNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            checkAndEnableSaveButton(binding)
            if (isChecked) {
                binding.aboutAuthorizationOnDeviceCheckBox.isEnabled = true
                binding.aboutNewProductsAndOffersCheckBox.isEnabled = true
            } else {
                binding.aboutAuthorizationOnDeviceCheckBox.isEnabled = false
                binding.aboutNewProductsAndOffersCheckBox.isEnabled = false
            }
        }
        binding.aboutAuthorizationOnDeviceCheckBox.setOnCheckedChangeListener { _, _ ->
            checkAndEnableSaveButton(
                binding
            )
        }
        binding.aboutNewProductsAndOffersCheckBox.setOnCheckedChangeListener { _, _ ->
            checkAndEnableSaveButton(
                binding
            )
        }
        binding.progressBar.progress = Random.nextInt(101)

        binding.saveButton.setOnClickListener {
            Toast.makeText(this, "Информация сохранена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndEnableSaveButton(binding: ActivityMainBinding) {
        val isNameValid =
            !binding.nameText.text.isNullOrEmpty() && binding.nameText.text!!.length <= 40
        val isPhoneValid = !binding.phoneTextView.text.isNullOrEmpty()
        val isGenderSelected =
            binding.maleRadioButton.isChecked || binding.femaleRadioButton.isChecked
        val isNotificationValid = !binding.reciveNotificationSwitch.isChecked ||
                (binding.aboutAuthorizationOnDeviceCheckBox.isChecked ||
                        binding.aboutNewProductsAndOffersCheckBox.isChecked)

        binding.saveButton.isEnabled =
            isNameValid && isPhoneValid && isGenderSelected && isNotificationValid
    }
}