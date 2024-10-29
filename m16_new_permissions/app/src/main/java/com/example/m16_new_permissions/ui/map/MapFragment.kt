package com.example.m16_new_permissions.ui.map

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.m16_new_permissions.data.repository.AttractionRepositoryImpl
import com.example.m16_new_permissions.data.api.AttractionApiService
import com.example.m16_new_permissions.databinding.FragmentMapBinding
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.presentation.viewmodel.AttractionsViewModel
import com.example.m16_new_permissions.presentation.viewmodel.AttractionsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController

    // Инициализируем Retrofit и репозиторий
    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.opentripmap.com/0.1/en/places/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AttractionApiService::class.java)
    }
    private val repository by lazy { AttractionRepositoryImpl(apiService) }

    private val attractionsViewModel: AttractionsViewModel by viewModels {
        AttractionsViewModelFactory(repository)
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val userLocation = GeoPoint(48.8584, 2.2945)  // заменить на текущее местоположение
            attractionsViewModel.loadAttractions(userLocation.latitude, userLocation.longitude)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Устанавливаем userAgentValue для osmdroid
        Configuration.getInstance().userAgentValue = requireContext().packageName

        // Создаем SharedPreferences с уникальным именем для osmdroid
        val context = requireContext()
        val prefs = context.getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        Configuration.getInstance().load(context, prefs)

        // Настраиваем кеширование карт
        val osmdroidBasePath = File(context.cacheDir, "osmdroid")
        val osmdroidTileCache = File(osmdroidBasePath, "tile")
        Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
        Configuration.getInstance().osmdroidTileCache = osmdroidTileCache

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller
        mapController.setZoom(15.0)

        // Подписка на изменения списка достопримечательностей
        viewLifecycleOwner.lifecycleScope.launch {
            attractionsViewModel.attractions.collectLatest { attractions ->
                attractions.forEach { attraction ->
                    addMarker(attraction)
                }
            }
        }

        return binding.root
    }

    private fun addMarker(attraction: Attraction) {
        val marker = Marker(mapView)
        marker.position = GeoPoint(attraction.point.lat, attraction.point.lon)
        marker.title = attraction.name
        mapView.overlays.add(marker)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.currentLocationButton.setOnClickListener {
            if (hasLocationPermission()) {
                val userLocation = GeoPoint(48.8584, 2.2945)  // заменить на текущее местоположение
                attractionsViewModel.loadAttractions(userLocation.latitude, userLocation.longitude)
            } else {
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding, чтобы избежать утечек памяти
    }
}