package com.example.m13_new_list.photoslist

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m13_new_list.PhotoDetailActivity
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
            // В списке грузим превью, оригинал остаётся для экрана деталей
            Glide.with(binding.root.context).load(photo.previewUrl).into(binding.imageView)
            binding.titleTextView.text = photo.title

            // Устанавливаем обработчик клика
            binding.root.setOnClickListener {
                onClick(photo)
                val context = binding.root.context
                val intent = Intent(context, PhotoDetailActivity::class.java).apply {
                    putExtra(PhotoDetailActivity.EXTRA_PHOTO_URL, photo.fullUrl)
                    putExtra(PhotoDetailActivity.EXTRA_TITLE, photo.title)
                    putExtra(PhotoDetailActivity.EXTRA_ROVER, photo.rover)
                    putExtra(PhotoDetailActivity.EXTRA_SOL, photo.sol)
                    putExtra(PhotoDetailActivity.EXTRA_CAMERA, photo.camera)
                    putExtra(PhotoDetailActivity.EXTRA_DATE, photo.date)
                    putExtra(PhotoDetailActivity.EXTRA_CREDIT, photo.credit)
                    putExtra(PhotoDetailActivity.EXTRA_DESCRIPTION, photo.description)
                }
                context.startActivity(intent)
            }
        }
    }
}
