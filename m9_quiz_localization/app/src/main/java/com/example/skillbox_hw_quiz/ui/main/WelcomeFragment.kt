package com.example.skillbox_hw_quiz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skillbox_hw_quiz.R
import com.example.skillbox_hw_quiz.databinding.FragmentWelcomeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    val dateFormat = SimpleDateFormat("dd-MM-yyyy")
    val calendar = Calendar.getInstance()
    val data = calendar.time

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dateOfBirthButton.setOnClickListener {
            val dataDialog = MaterialDatePicker.Builder.datePicker()
                .build()
            dataDialog.addOnPositiveButtonClickListener { timeInMillis ->
                // Используйте выбранное значение для установки времени в календарь
                calendar.timeInMillis = timeInMillis
                // Форматирование и отображение выбранной даты
                val selectedDate = dateFormat.format(calendar.time)
                Snackbar.make(binding.root, selectedDate, Snackbar.LENGTH_SHORT).show()
            }

            dataDialog.show(parentFragmentManager, "datePicker")
        }

        binding.continueButton.setOnClickListener {
            // Здесь реализуем переход к следующему экрану
            findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}