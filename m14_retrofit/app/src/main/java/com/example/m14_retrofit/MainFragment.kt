package com.example.m14_retrofit

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val imageView = requireActivity().findViewById<ImageView>(R.id.imageViewProfile)
        val imageView = binding.imageViewProfile
        val name = binding.textViewName


        lifecycleScope.launch {

            val response = RetrofitService.searchRandomuserApi.getRandomUser()
            imageView.load(response.body()?.first()?.url)

            }
        }
    }
}