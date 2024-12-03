package com.example.m17_new_firebase.ui.map

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.m17_new_firebase.MainActivity
import com.example.m17_new_firebase.R
import com.example.m17_new_firebase.databinding.FragmentMapBinding
//import com.example.m17_new_firebase.R
import com.example.m17_new_firebase.data.repository.AttractionRepositoryImpl
import com.example.m17_new_firebase.data.service.LocationService
//import com.example.m17_new_firebase.databinding.FragmentMapBinding
import com.example.m17_new_firebase.domain.model.Attraction
import com.example.m17_new_firebase.domain.repository.AttractionRepository
import com.example.m17_new_firebase.domain.service.ILocationService
import com.example.m17_new_firebase.presentation.viewmodel.MapViewModel
import com.example.m17_new_firebase.presentation.viewmodel.MapViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay

    private val locationService: ILocationService by lazy { LocationService(requireContext()) }
    private val attractionRepository: AttractionRepository by lazy { AttractionRepositoryImpl() }

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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Установка пользовательского агента для osmdroid
        Configuration.getInstance().userAgentValue = requireContext().packageName

        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // Настройка MapView и контроллера
        mapView = binding.mapView
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
//                throw Exception("My first Exception")
                createNotification()
                mapViewModel.updateCurrentLocation(locationOverlay.myLocation)
                mapController.setCenter(locationOverlay.myLocation)
                mapController.setZoom(15.0) // Обновляем масштаб при переходе к текущей локации
            } else {
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()

        // Получение токена FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                // Отправьте токен на ваш сервер, если требуется
            } else {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
            }
        }
    }

    // Функция для масштабирования иконки маркера
    private fun getScaledMarkerIcon(): BitmapDrawable? {
        val drawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.custom_marker) as? BitmapDrawable
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
            return
        }

        mapView.overlays.removeIf { it is Marker }

        attractions.forEach { attraction ->
            Log.d(
                "MapFragment",
                "Adding marker at ${attraction.latitude}, ${attraction.longitude} for ${attraction.name}"
            )
            val marker = Marker(mapView).apply {
                position = GeoPoint(attraction.latitude, attraction.longitude)
                title = attraction.name
                snippet = attraction.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = getScaledMarkerIcon()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding, чтобы избежать утечек памяти
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name = "Test notification channel"
        val descriptionText = "This is test description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManger =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManger.createNotificationChannel(channel)
    }

    fun createNotification() {

        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        else
            PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val notificaton = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("My first notification")
            .setContentText("Description of my first notification")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(requireContext()).notify(NOTIFICATION_ID, notificaton)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "test_channel_id"
        private const val NOTIFICATION_ID = 1000
    }
}