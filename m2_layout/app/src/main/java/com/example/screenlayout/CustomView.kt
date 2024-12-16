package com.example.screenlayout

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.screenlayout.databinding.CustomViewBinding

class CustomView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) :
    LinearLayout(context, attributeSet) {
    private val binding: CustomViewBinding
    init {
        val inflatedView = inflate(context, R.layout.custom_view, this)
        binding = CustomViewBinding.bind(inflatedView)
    }

    fun setFirstText(text: String) {
        binding.firstText.text = text
    }

    fun setSecondText(text: String) {
        binding.secondText.text = text
    }
}