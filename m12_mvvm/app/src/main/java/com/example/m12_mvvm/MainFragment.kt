package com.example.m12_mvvm

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.m12_mvvm.databinding.FragmentMainBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchButton.setOnClickListener { viewModel.search() }
        viewLifecycleOwner.lifecycleScope
            .launchWhenStarted {
                viewModel.stateFlow
                    .collect {
                        Log.d(TAG, "onViewCreated $it")
                        binding.searchTextResult.text = it
                    }
            }
        // Наблюдение за изменением состояния доступности кнопки поиска
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchEnabled.collect { isEnabled ->
                binding.searchButton.isEnabled = isEnabled
            }

        }

        // Обработчик изменения текста в поле ввода
        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Перед изменением текста
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Во время изменения текста
            }

            override fun afterTextChanged(s: Editable) {
                // После изменения текста
                viewModel.setSearchText(s.toString())
            }
        })
    }
}