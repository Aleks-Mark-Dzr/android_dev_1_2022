package com.example.m16_new_permissions.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import java.util.Locale

class MapFragment : Fragment() {

    private companion object {
        const val MY_LOCATION_ICON_SIZE_DP = 24
        const val MAX_LATITUDE = 90.0
        const val MAX_LONGITUDE = 180.0
    }

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

    // Перетаскивание метки пользователя по карте — быстрый способ поправить её локацию
    private val userMarkerDragListener = object : Marker.OnMarkerDragListener {
        override fun onMarkerDragStart(marker: Marker) = Unit

        override fun onMarkerDrag(marker: Marker) = Unit

        override fun onMarkerDragEnd(marker: Marker) {
            val attraction = marker.relatedObject as? Attraction ?: return
            mapViewModel.updateAttraction(
                attraction,
                attraction.name,
                attraction.description,
                marker.position
            )
            Toast.makeText(
                requireContext(),
                getString(R.string.marker_moved, attraction.name),
                Toast.LENGTH_SHORT
            ).show()
        }
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
        applyRedMyLocationIcons()
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
        showAttractionDialog(currentLocation)
    }

    /**
     * Диалог названия, описания и координат метки.
     * Без [attraction] создаёт новую метку, с ним — редактирует сохранённую и позволяет её удалить.
     */
    private fun showAttractionDialog(geoPoint: GeoPoint, attraction: Attraction? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_attraction, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val latitudeEditText = dialogView.findViewById<EditText>(R.id.latitudeEditText)
        val longitudeEditText = dialogView.findViewById<EditText>(R.id.longitudeEditText)

        nameEditText.setText(attraction?.name.orEmpty())
        descriptionEditText.setText(attraction?.description.orEmpty())
        latitudeEditText.setText(formatCoordinate(geoPoint.latitude))
        longitudeEditText.setText(formatCoordinate(geoPoint.longitude))
        if (attraction != null) {
            dialogView.findViewById<TextView>(R.id.dragHintTextView).visibility = View.VISIBLE
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle(
                if (attraction == null) R.string.add_marker_dialog_title
                else R.string.edit_marker_dialog_title
            )
            .setView(dialogView)
            // Слушатель назначаем ниже, иначе диалог закроется даже при пустом названии
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
        if (attraction != null) {
            builder.setNeutralButton(R.string.action_delete, null)
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameEditText.text.toString().trim()
                if (name.isEmpty()) {
                    nameEditText.error = getString(R.string.attraction_name_required)
                    return@setOnClickListener
                }

                val latitude = parseCoordinate(
                    latitudeEditText,
                    MAX_LATITUDE,
                    R.string.attraction_latitude_invalid
                ) ?: return@setOnClickListener
                val longitude = parseCoordinate(
                    longitudeEditText,
                    MAX_LONGITUDE,
                    R.string.attraction_longitude_invalid
                ) ?: return@setOnClickListener

                val description = descriptionEditText.text.toString().trim()
                val position = GeoPoint(latitude, longitude)
                if (attraction == null) {
                    mapViewModel.addAttraction(name, description, position)
                    showToast(getString(R.string.marker_added, name))
                } else {
                    mapViewModel.updateAttraction(attraction, name, description, position)
                    showToast(getString(R.string.marker_updated, name))
                }
                dialog.dismiss()
            }

            if (attraction != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    dialog.dismiss()
                    showDeleteConfirmationDialog(attraction)
                }
            }
        }

        dialog.show()
    }

    // Удаление необратимо, поэтому спрашиваем подтверждение
    private fun showDeleteConfirmationDialog(attraction: Attraction) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_marker_dialog_title)
            .setMessage(getString(R.string.delete_marker_confirmation, attraction.name))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                mapViewModel.deleteAttraction(attraction)
                showToast(getString(R.string.marker_deleted, attraction.name))
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    // Координаты показываем в виде, который сами же умеем разобрать обратно
    private fun formatCoordinate(value: Double): String = String.format(Locale.US, "%.6f", value)

    private fun parseCoordinate(
        editText: EditText,
        limit: Double,
        @StringRes errorRes: Int
    ): Double? {
        // Запятую с клавиатуры принимаем наравне с точкой
        val value = editText.text.toString().trim().replace(',', '.').toDoubleOrNull()
        if (value == null || value < -limit || value > limit) {
            editText.error = getString(errorRes)
            return null
        }
        return value
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Своя геопозиция выделяется красным: точка на месте и красная стрелка при движении
    private fun applyRedMyLocationIcons() {
        val sizePx = (MY_LOCATION_ICON_SIZE_DP * resources.displayMetrics.density).toInt()

        getBitmapFromDrawable(R.drawable.my_location_dot, sizePx)?.let { personBitmap ->
            locationOverlay.setPersonIcon(personBitmap)
            locationOverlay.setPersonAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }

        getBitmapFromDrawable(R.drawable.red_marker, sizePx)?.let { directionBitmap ->
            locationOverlay.setDirectionIcon(directionBitmap)
            locationOverlay.setDirectionAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }

    // Векторные иконки OSM принимает только как Bitmap, поэтому отрисовываем их вручную
    private fun getBitmapFromDrawable(@DrawableRes drawableRes: Int, sizePx: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableRes) ?: return null
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(Canvas(bitmap))
        return bitmap
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

    // Функция для добавления маркеров для всех достопримечательностей на карту
    private fun addAttractionsToMap(attractions: List<Attraction>) {
        if (attractions.isEmpty()) {
            Log.e("MapFragment", "No attractions found to display on the map.")
        }

        mapView.overlays.removeIf { it is Marker }

        attractions.forEach { attraction ->
            Log.d("MapFragment", "Adding marker at ${attraction.latitude}, ${attraction.longitude} for ${attraction.name}")
            val marker = Marker(mapView).apply {
                position = GeoPoint(attraction.latitude, attraction.longitude)
                title = attraction.name
                snippet = attraction.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                // Сохранённые пользователем метки рисуем так же, как существующие
                icon = getScaledMarkerIcon()

                // Редактировать можно только свои метки: по нажатию открываем диалог,
                // а долгим нажатием метку разрешено перетащить в другую точку
                if (attraction.isUserAdded) {
                    relatedObject = attraction
                    isDraggable = true
                    setOnMarkerClickListener { clickedMarker, _ ->
                        showAttractionDialog(clickedMarker.position, attraction)
                        true
                    }
                    setOnMarkerDragListener(userMarkerDragListener)
                }
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
