package com.example.m13_new_list

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.m13_new_list.databinding.ItemDetailFotoBinding

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ItemDetailFotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Используем ViewBinding для связывания с макетом
        binding = ItemDetailFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем переданные данные о фото
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)

        // Загружаем фото с помощью Glide
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUrl)
                .into(binding.detailFotoView)
        }

        // Набор полей у источников разный, поэтому пустые прячем, а не показываем как "Sol: "
        binding.titleTextView.bindOrHide(intent.getStringExtra(EXTRA_TITLE))
        binding.roverNameTextView.bindOrHide(intent.getStringExtra(EXTRA_ROVER), R.string.detail_rover)
        binding.solTextView.bindOrHide(intent.getStringExtra(EXTRA_SOL), R.string.detail_sol)
        binding.cameraTextView.bindOrHide(intent.getStringExtra(EXTRA_CAMERA), R.string.detail_camera)
        binding.dateTextView.bindOrHide(intent.getStringExtra(EXTRA_DATE), R.string.detail_date)
        binding.creditTextView.bindOrHide(intent.getStringExtra(EXTRA_CREDIT), R.string.detail_credit)
        binding.descriptionTextView.bindOrHide(intent.getStringExtra(EXTRA_DESCRIPTION))
    }

    private fun TextView.bindOrHide(value: String?, @StringRes format: Int? = null) {
        if (value.isNullOrBlank()) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = if (format == null) value else getString(format, value)
        }
    }

    companion object {
        const val EXTRA_PHOTO_URL = "photo_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ROVER = "rover_name"
        const val EXTRA_SOL = "sol"
        const val EXTRA_CAMERA = "camera_name"
        const val EXTRA_DATE = "date"
        const val EXTRA_CREDIT = "credit"
        const val EXTRA_DESCRIPTION = "description"
    }
}
