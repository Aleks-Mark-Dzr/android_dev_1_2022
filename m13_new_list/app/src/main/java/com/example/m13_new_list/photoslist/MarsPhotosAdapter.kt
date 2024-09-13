package com.example.m13_new_list.photoslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m13_new_list.databinding.ItemPhotoBinding
import com.example.m13_new_list.models.Photo

class MarsPhotosAdapter(private val photos: List<Photo>, private val onClick: (Photo) -> Unit) :
    RecyclerView.Adapter<MarsPhotosAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        // Инициализация binding здесь с использованием инфлейта макета
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount() = photos.size

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photo) {
            // Использование binding для обращения к view
            Glide.with(binding.root.context).load(photo.img_src).into(binding.imageView)
            binding.root.setOnClickListener { onClick(photo) }
        }
    }
}