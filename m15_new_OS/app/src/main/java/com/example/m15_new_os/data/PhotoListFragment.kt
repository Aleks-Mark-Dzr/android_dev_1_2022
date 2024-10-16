package com.example.m15_new_os.data

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.m15_new_os.R
import com.example.m15_new_os.databinding.FragmentPhotoListBinding
import com.example.m15_new_os.models.PhotoEntity
import com.example.m15_new_os.presentation.PhotoViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PhotoListFragment : Fragment(R.layout.fragment_photo_list) {

    private val viewModel: PhotoViewModel by viewModels()
    private var _binding: FragmentPhotoListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PhotoAdapter
    private lateinit var photoUri: Uri

    // Запрос разрешения на использование камеры
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Log.e("PhotoListFragment", "Camera permission denied")
        }
    }

    // Запуск камеры для съемки фото
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val photoEntity = PhotoEntity(
                photoUri = photoUri.toString(),
                dateTaken = System.currentTimeMillis()
            )
            viewModel.insert(photoEntity)
        } else {
            Log.e("PhotoListFragment", "Photo capture failed or canceled")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPhotoListBinding.bind(view)

        // Настраиваем RecyclerView
        adapter = PhotoAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerView.adapter = adapter

        // Подписываемся на данные из ViewModel и обновляем адаптер
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allPhotos.collect { photos ->
                adapter.submitList(photos)
            }
        }

        // Устанавливаем обработчик для кнопки добавления фото
        binding.buttonTakePhoto.setOnClickListener {
            Log.d("PhotoListFragment", "Take photo button clicked")
            // Проверяем разрешение на использование камеры
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                // Запрашиваем разрешение
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        // Создаем URI для сохранения фото
        photoUri = createImageUri() ?: return
        // Запускаем камеру с этим URI
        takePictureLauncher.launch(photoUri)
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        return requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )?.also {
            Log.d("PhotoListFragment", "URI created: $it")
        } ?: run {
            Log.e("PhotoListFragment", "Failed to create URI")
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}