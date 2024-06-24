package com.example.m14_retrofit

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.m14_retrofit.databinding.FragmentMainBinding
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private val TAG = "MainViewModel"

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Наблюдаем за изменениями данных пользователя
        viewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            if (userData != null) {
                binding.textViewName.text = "Полное имя: ${userData.fullName}"
                binding.textViewEmail.text = "Email: ${userData.email}"
                binding.textViewPhone.text = "Тлф: ${userData.phone}"
                binding.textViewCell.text = "Cell: ${userData.cell}"
                binding.textViewLocation.text = "Локация: ${userData.location}"
                Glide.with(this).load(userData.pictureUrl).into(binding.imageViewProfile)
            } else {
                Log.e(TAG, "Не удалось загрузить данные пользователя")
            }
        })

        // Запускаем загрузку данных
        viewModel.fetchRandomUser()

        // Обработчик нажатия кнопки searchButton
        binding.searchButton.setOnClickListener {
            Log.d(TAG, "Кнопка поиска нажата")
            viewModel.fetchRandomUser()
        }
    }
}
