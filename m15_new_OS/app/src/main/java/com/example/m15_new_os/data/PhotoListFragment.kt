package com.example.m15_new_os.data

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m15_new_os.R
import com.example.m15_new_os.presentation.PhotoViewModel

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.m15_new_os.databinding.FragmentPhotoListBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PhotoListFragment : Fragment(R.layout.fragment_photo_list) {

    private val viewModel: PhotoViewModel by viewModels()
    private var _binding: FragmentPhotoListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PhotoListFragment", "onViewCreated called")

        _binding = FragmentPhotoListBinding.bind(view)
        Log.d("PhotoListFragment", "Binding initialized")

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        Log.d("PhotoListFragment", "RecyclerView layout manager set")

        val adapter = PhotoAdapter()
        recyclerView.adapter = adapter
        Log.d("PhotoListFragment", "RecyclerView adapter set")

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allPhotos.collect { photos ->
                Log.d("PhotoListFragment", "Collecting photos: ${photos.size} items")
                adapter.submitList(photos)
            }
        }

        val fab = binding.buttonTakePhoto
        fab.setOnClickListener {
            Log.d("PhotoListFragment", "FAB clicked, navigating to AddPhotoFragment")
            findNavController().navigate(R.id.action_photoListFragment_to_addPhotoFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}