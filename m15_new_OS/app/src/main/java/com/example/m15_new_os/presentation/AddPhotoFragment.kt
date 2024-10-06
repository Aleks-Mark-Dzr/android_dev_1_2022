package com.example.m15_new_os.presentation

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.m15_new_os.R
import com.example.m15_new_os.models.PhotoEntity

class AddPhotoFragment : Fragment() {

    private val viewModel: PhotoViewModel by viewModels()
    private lateinit var photoUri: Uri

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val photoEntity = PhotoEntity(
                photoUri = photoUri.toString(),
                dateTaken = System.currentTimeMillis()
            )
            viewModel.insert(photoEntity)
            findNavController().navigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonTakePhoto = view.findViewById<Button>(R.id.button_take_photo)
        buttonTakePhoto.setOnClickListener {
            val uri = createImageUri()
            photoUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!
    }
}