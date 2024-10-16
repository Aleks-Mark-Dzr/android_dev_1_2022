package com.example.m15_new_os.data

import android.view.LayoutInflater
import android.view.ViewGroup
import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m15_new_os.databinding.ListItemPhotoBinding
import com.example.m15_new_os.models.PhotoEntity
import java.text.SimpleDateFormat
import java.util.*

class PhotoAdapter : ListAdapter<PhotoEntity, PhotoAdapter.PhotoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ListItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo)
    }

    class PhotoViewHolder(private val binding: ListItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: PhotoEntity) {
            // Загружаем фото через Glide
            Glide.with(itemView.context)
                .load(Uri.parse(photo.photoUri))
                .into(binding.imageView)

            // Форматируем дату и отображаем
            val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(photo.dateTaken))
            binding.dateTaken.text = formattedDate
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PhotoEntity>() {
        override fun areItemsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity): Boolean {
            return oldItem == newItem
        }
    }
}