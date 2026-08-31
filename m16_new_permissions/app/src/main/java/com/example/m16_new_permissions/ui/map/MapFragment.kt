package com.example.m16_new_permissions.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.m16_new_permissions.R
import com.example.m16_new_permissions.data.local.AttractionLocalDataSource
import com.example.m16_new_permissions.data.repository.AttractionRepositoryImpl
import com.example.m16_new_permissions.data.service.LocationService
import com.example.m16_new_permissions.databinding.FragmentMapBinding
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.repository.AttractionRepository
import com.example.m16_new_permissions.domain.service.ILocationService
import com.example.m16_new_permissions.presentation.viewmodel.MapViewModel
import com.example.m16_new_permissions.presentation.viewmodel.MapViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay

    // Разрешение запрашивалось ради добавления метки: после выдачи сразу открываем диалог
    private var pendingAddMarker = false

    private val locationService: ILocationService by lazy { LocationService(requireContext()) }
    private val attractionRepository: AttractionRepository by lazy {
        AttractionRepositoryImpl(AttractionLocalDataSource(requireContext()))
    }

    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory(locationService, attractionRepository)
    }

    // Регистрация обработчика разрешений
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            enableMyLocationIfPermitted()
            if (pendingAddMarker) {
                addMarkerAtCurrentLocation()
            }
        }
        pendingAddMarker = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // Настройка MapView и контроллера
        // User-Agent задаётся один раз в M16NewPermissionsApp: перезаписывать его здесь нельзя,
        // иначе OSM отдаёт тайлы-заглушки "Access blocked".
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller
        mapController.setZoom(5.0)
        mapController.setCenter(GeoPoint(48.8584, 2.2945)) // Центрируйте на интересующей области

        // Настройка слоя для отображения текущего местоположения
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        mapView.overlays.add(locationOverlay)

        // Включаем отображение местоположения при наличии разрешений
        enableMyLocationIfPermitted()

        // Подписка на обновления текущего местоположения
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                mapViewModel.currentLocation.collectLatest { geoPoint ->
                    geoPoint?.let {
                        mapController.setCenter(it)
                        mapController.setZoom(15.0) // Устанавливаем масштаб для отображения текущей локации
                    }
                }
            }
        }

        // Подписка на обновления списка достопримечательностей
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                mapViewModel.attractions.collectLatest { attractions ->
                    addAttractionsToMap(attractions)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка кнопок зума и кнопки текущего местоположения
        binding.zoomInButton.setOnClickListener { mapController.zoomIn() }
        binding.zoomOutButton.setOnClickListener { mapController.zoomOut() }
        binding.currentLocationButton.setOnClickListener {
            if (hasLocationPermission()) {
                locationOverlay.myLocation?.let { currentLocation ->
                    mapViewModel.updateCurrentLocation(currentLocation)
                    mapController.setCenter(currentLocation)
                    mapController.setZoom(15.0)
                } // Обновляем масштаб при переходе к текущей локации
            } else {
                requestLocationPermission()
            }
        }

        // Добавление метки с текущей геопозицией и описанием
        binding.addMarkerButton.setOnClickListener {
            if (hasLocationPermission()) {
                addMarkerAtCurrentLocation()
            } else {
                pendingAddMarker = true
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Определяем текущую геопозицию и предлагаем описать новую метку
    private fun addMarkerAtCurrentLocation() {
        val currentLocation = locationOverlay.myLocation ?: mapViewModel.resolveCurrentLocation()
        if (currentLocation == null) {
            Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        mapViewModel.updateCurrentLocation(currentLocation)
        showAddAttractionDialog(currentLocation)
    }

    // Диалог ввода названия и описания метки
    private fun showAddAttractionDialog(geoPoint: GeoPoint) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_attraction, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        dialogView.findViewById<TextView>(R.id.coordinatesTextView).text =
            getString(R.string.add_marker_coordinates, geoPoint.latitude, geoPoint.longitude)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_marker_dialog_title)
            .setView(dialogView)
            // Слушатель назначаем ниже, иначе диалог закроется даже при пустом названии
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameEditText.text.toString().trim()
                if (name.isEmpty()) {
                    nameEditText.error = getString(R.string.attraction_name_required)
                    return@setOnClickListener
                }

                mapViewModel.addAttraction(name, descriptionEditText.text.toString().trim(), geoPoint)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.marker_added, name),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // Функция для масштабирования иконки маркера
    private fun getScaledMarkerIcon(): BitmapDrawable? {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.custom_marker) as? BitmapDrawable
        drawable?.let {
            val bitmap = it.bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, false)
            return BitmapDrawable(resources, scaledBitmap)
        }
        return null
    }

    // Метки, добавленные пользователем, выделяем отдельной иконкой
    private fun getMarkerIcon(attraction: Attraction): Drawable? {
        return if (attraction.isUserAdded) {
            ContextCompat.getDrawable(requireContext(), R.drawable.red_marker)
        } else {
            getScaledMarkerIcon()
        }
    }

    // Функция для добавления маркеров для всех достопримечательностей на карту
    private fun addAttractionsToMap(attractions: List<Attraction>) {
        if (attractions.isEmpty()) {
            Log.e("MapFragment", "No attractions found to display on the map.")
            return
        }

        mapView.overlays.removeIf { it is Marker }

        attractions.forEach { attraction ->
            Log.d("MapFragment", "Adding marker at ${attraction.latitude}, ${attraction.longitude} for ${attraction.name}")
            val marker = Marker(mapView).apply {
                position = GeoPoint(attraction.latitude, attraction.longitude)
                title = attraction.name
                snippet = attraction.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = getMarkerIcon(attraction)
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    private fun enableMyLocationIfPermitted() {
        if (hasLocationPermission()) {
            locationOverlay.enableMyLocation()
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
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        locationOverlay.disableMyLocation()
        mapView.onDetach()
        super.onDestroyView()
        _binding = null // Очищаем binding, чтобы избежать утечек памяти
    }
}
