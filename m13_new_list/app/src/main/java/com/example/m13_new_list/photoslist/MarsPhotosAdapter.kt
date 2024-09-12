package com.example.m13_new_list.photoslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m13_new_list.R
import com.example.m13_new_list.databinding.ItemPhotoBinding
import com.example.m13_new_list.models.Photo

//import kotlinx.android.synthetic.main.item_photo.view.*

class MarsPhotosAdapter(private val photos: List<Photo>, private val onClick: (Photo) -> Unit) :
    RecyclerView.Adapter<MarsPhotosAdapter.PhotoViewHolder>() {

    private lateinit var binding: ItemPhotoBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount() = photos.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(photo: Photo) {
            Glide.with(itemView.context).load(photo.img_src).into(binding.imageView)
            itemView.setOnClickListener { onClick(photo) }
        }
    }
}