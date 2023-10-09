package com.example.m2_layout

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.m2_layout.databinding.CustomViewBinding

class CustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val binding: CustomViewBinding

    init {
        val inflatedView = inflate(context, R.layout.custom_view, this)
        binding = CustomViewBinding.bind(inflatedView)
    }

    fun setTopStringText(message: String) {
        binding.topString.text = message
    }

    fun setBottomStringText(message: String) {
        binding.bottomString.text = message
    }
}