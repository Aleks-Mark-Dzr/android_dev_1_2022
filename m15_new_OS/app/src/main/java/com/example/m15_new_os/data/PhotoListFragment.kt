package com.example.m15_new_os.data

import android.os.Bundle
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
    private lateinit var binding: FragmentPhotoListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация View Binding
        binding = FragmentPhotoListBinding.bind(view)

//        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        val adapter = PhotoAdapter()
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allPhotos.collect { photos ->
                adapter.submitList(photos)
            }
        }

//        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_photo)
        val fab = binding.fabAddPhoto
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_photoListFragment_to_addPhotoFragment)
        }
    }
}