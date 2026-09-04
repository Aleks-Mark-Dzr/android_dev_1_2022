package com.example.m16_new_permissions.ui.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.m16_new_permissions.R
import com.example.m16_new_permissions.data.backup.AttractionBackup
import com.example.m16_new_permissions.data.local.AttractionLocalDataSource
import com.example.m16_new_permissions.data.local.AttractionPhotoStorage
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
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapFragment : Fragment() {

    private companion object {
        const val MY_LOCATION_ICON_SIZE_DP = 24
        const val MAX_LATITUDE = 90.0
        const val MAX_LONGITUDE = 180.0
        // Размеры, до которых уменьшаем фотографию при показе: в диалоге и в полноэкранном просмотре
        const val PHOTO_PREVIEW_MAX_SIZE_PX = 720
        const val PHOTO_FULL_MAX_SIZE_PX = 1600
        const val BACKUP_MIME_TYPE = "application/zip"
        // Диск и часть файловых менеджеров отдают zip под другим типом, поэтому принимаем все три
        val BACKUP_OPEN_MIME_TYPES = arrayOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/octet-stream"
        )
        const val BACKUP_NAME_DATE_PATTERN = "yyyyMMdd_HHmm"
        // Приближение, с которым показываем найденную поиском метку
        const val SEARCH_RESULT_ZOOM = 17.0
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay

    // Разрешение запрашивалось ради добавления метки: после выдачи сразу открываем диалог
    private var pendingAddMarker = false

    // Метки на карте по идентификатору: по нему поиск находит, чью подпись раскрыть
    private val markersByAttractionId = mutableMapOf<String, Marker>()

    // Файл, в который камера сейчас делает снимок
    private var pendingCameraPhotoPath: String? = null

    // Открытый диалог метки: именно ему принадлежит выбранная фотография
    private var activePhotoController: AttractionPhotoController? = null

    private val locationService: ILocationService by lazy { LocationService(requireContext()) }
    private val photoStorage: AttractionPhotoStorage by lazy { AttractionPhotoStorage(requireContext()) }
    private val attractionRepository: AttractionRepository by lazy {
        val localDataSource = AttractionLocalDataSource(requireContext())
        AttractionRepositoryImpl(
            localDataSource,
            photoStorage,
            AttractionBackup(requireContext(), localDataSource, photoStorage)
        )
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

    // Разрешение на камеру спрашиваем только когда пользователь выбрал съёмку фотографии
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            showToast(getString(R.string.camera_permission_required))
        }
    }

    // Снимок камеры пишется сразу в подготовленный файл, сюда приходит только признак успеха
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val path = pendingCameraPhotoPath
        pendingCameraPhotoPath = null
        val controller = activePhotoController
        if (success && path != null && controller != null) {
            controller.setPhoto(path)
        } else {
            // Съёмку отменили или диалог уже закрыт — пустой файл не нужен
            photoStorage.deleteFile(path)
        }
    }

    // Изображение из галереи копируем к себе: чужая content-ссылка после перезапуска перестанет открываться
    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        val controller = activePhotoController ?: return@registerForActivityResult
        if (uri == null) return@registerForActivityResult

        val copiedPath = photoStorage.copyToTemp(uri)
        if (copiedPath == null) {
            showToast(getString(R.string.photo_save_failed))
        } else {
            controller.setPhoto(copiedPath)
        }
    }

    // Куда сохранить архив, решает пользователь в системном диалоге: это может быть и папка Google Диска
    private val exportBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE)
    ) { uri ->
        uri?.let { mapViewModel.exportBackup(it) }
    }

    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { mapViewModel.importBackup(it) }
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

        // Итог работы с резервной копией показываем тостом
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                mapViewModel.backupEvents.collect { event -> showBackupResult(event) }
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

        binding.backupButton.setOnClickListener { showBackupDialog() }

        setupSearch()
    }

    /**
     * Поиск метки по названию и описанию: пока пользователь набирает символы, под полем
     * показываются подходящие метки, а по выбору одной из них карта переходит к её геопозиции.
     */
    private fun setupSearch() {
        val searchField = binding.searchAutoCompleteTextView
        // Варианты подбирает ViewModel — она знает актуальный список меток
        val suggestionAdapter = AttractionSuggestionAdapter(
            requireContext(),
            mapViewModel::findAttractions
        )
        searchField.setAdapter(suggestionAdapter)

        searchField.setOnItemClickListener { _, _, position, _ ->
            suggestionAdapter.getItem(position)?.let { showAttractionOnMap(it) }
        }

        // По кнопке «Поиск» на клавиатуре переходим к первому подходящему варианту
        searchField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false

            val match = mapViewModel.findAttractions(searchField.text.toString()).firstOrNull()
            if (match == null) {
                showToast(getString(R.string.search_attraction_not_found))
            } else {
                // Без второго аргумента подстановка названия снова откроет список подсказок
                searchField.setText(match.name, false)
                searchField.dismissDropDown()
                showAttractionOnMap(match)
            }
            true
        }

        searchField.doAfterTextChanged { text ->
            binding.clearSearchButton.visibility =
                if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        binding.clearSearchButton.setOnClickListener {
            searchField.setText("", false)
            searchField.dismissDropDown()
            hideKeyboard(searchField)
            searchField.clearFocus()
        }
    }

    // Переход к выбранной в поиске метке: клавиатура и список подсказок уступают место карте
    private fun showAttractionOnMap(attraction: Attraction) {
        val searchField = binding.searchAutoCompleteTextView
        hideKeyboard(searchField)
        searchField.clearFocus()

        mapController.setZoom(SEARCH_RESULT_ZOOM)
        mapController.animateTo(GeoPoint(attraction.latitude, attraction.longitude))

        // Раскрываем подпись найденной метки, иначе среди соседних её не отличить
        InfoWindow.closeAllInfoWindowsOn(mapView)
        markersByAttractionId[attraction.id]?.showInfoWindow()
        mapView.invalidate()
    }

    private fun hideKeyboard(view: View) {
        ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Выбор действия с резервной копией: выгрузить метки в файл или восстановить их из файла.
     * Действия разведены по кнопкам, а не по списку: список и пояснение занимают в диалоге
     * одно и то же место, и вместе они не показываются.
     */
    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.backup_dialog_title)
            .setMessage(R.string.backup_dialog_message)
            .setPositiveButton(R.string.backup_action_export) { _, _ -> startBackupExport() }
            .setNeutralButton(R.string.backup_action_import) { _, _ -> startBackupImport() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun startBackupExport() {
        // Предустановленные достопримечательности заданы в коде и в копию не идут
        if (mapViewModel.attractions.value.none { it.isUserAdded }) {
            showToast(getString(R.string.backup_nothing_to_export))
            return
        }

        val timestamp = SimpleDateFormat(BACKUP_NAME_DATE_PATTERN, Locale.US).format(Date())
        launchBackupPicker { exportBackupLauncher.launch(getString(R.string.backup_file_name, timestamp)) }
    }

    private fun startBackupImport() {
        launchBackupPicker { importBackupLauncher.launch(BACKUP_OPEN_MIME_TYPES) }
    }

    // На устройстве без файлового менеджера системный диалог открыть некому
    private fun launchBackupPicker(launch: () -> Unit) {
        try {
            launch()
        } catch (e: ActivityNotFoundException) {
            Log.e("MapFragment", "No document picker available", e)
            showToast(getString(R.string.backup_no_file_manager))
        }
    }

    private fun showBackupResult(event: MapViewModel.BackupEvent) {
        val message = when (event) {
            is MapViewModel.BackupEvent.Exported ->
                getString(R.string.backup_exported, event.count)

            is MapViewModel.BackupEvent.Imported ->
                getString(R.string.backup_imported, event.added, event.updated)

            MapViewModel.BackupEvent.ExportFailed -> getString(R.string.backup_export_failed)
            MapViewModel.BackupEvent.ImportFailed -> getString(R.string.backup_import_failed)
        }
        showToast(message)
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
     * Диалог названия, описания, координат и фотографии метки.
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

        // Фотографией диалога управляет отдельный контроллер: он же убирает за собой временные файлы
        val photoController = AttractionPhotoController(dialogView, attraction?.photoName)
        activePhotoController = photoController

        // Делимся тем, что сейчас в полях диалога: метку не обязательно сначала сохранять
        dialogView.findViewById<Button>(R.id.shareButton).setOnClickListener {
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

            shareAttraction(
                name,
                descriptionEditText.text.toString().trim(),
                GeoPoint(latitude, longitude),
                photoController.currentPhotoPath
            )
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
                // Фото переносим в постоянное хранилище только сейчас, когда метка действительно сохраняется
                val photoName = photoController.commit()
                if (attraction == null) {
                    mapViewModel.addAttraction(name, description, position, photoName)
                    showToast(getString(R.string.marker_added, name))
                } else {
                    mapViewModel.updateAttraction(attraction, name, description, position, photoName)
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

        dialog.setOnDismissListener {
            photoController.discardUncommittedPhoto()
            if (activePhotoController === photoController) {
                activePhotoController = null
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

    /**
     * Фотография метки внутри диалога: выбор источника, предпросмотр, удаление
     * и уборка временных файлов, если метку в итоге не сохранили.
     */
    private inner class AttractionPhotoController(
        dialogView: View,
        private val savedPhotoName: String?
    ) {
        private val photoImageView: ImageView = dialogView.findViewById(R.id.photoImageView)
        private val photoHintTextView: TextView = dialogView.findViewById(R.id.photoHintTextView)
        private val addPhotoButton: Button = dialogView.findViewById(R.id.addPhotoButton)
        private val removePhotoButton: Button = dialogView.findViewById(R.id.removePhotoButton)

        // Фото, выбранное в диалоге: полный путь, потому что пока метка не сохранена,
        // это может быть временный файл в кэше
        private var photoPath: String? = photoStorage.pathOf(savedPhotoName)

        // После сохранения метки временный файл трогать нельзя — он уже стал фотографией метки
        private var isCommitted = false

        /** Фотография, которую диалог показывает прямо сейчас: ею и делимся */
        val currentPhotoPath: String?
            get() = photoPath

        init {
            addPhotoButton.setOnClickListener { showPhotoSourceDialog() }
            removePhotoButton.setOnClickListener {
                setPhoto(null)
                showToast(getString(R.string.photo_removed))
            }
            photoImageView.setOnClickListener {
                photoPath?.let { showPhotoPreviewDialog(it) }
            }
            bindPhoto()
        }

        fun setPhoto(path: String?) {
            val previous = photoPath
            // Заменённый или убранный временный файл сразу удаляем: в метку он уже не попадёт
            if (previous != null && previous != path && photoStorage.isTemporary(previous)) {
                photoStorage.deleteFile(previous)
            }
            photoPath = path
            bindPhoto()
        }

        /** Закрепляет фотографию за меткой и возвращает имя файла, которое нужно сохранить */
        fun commit(): String? {
            val current = photoPath
            isCommitted = true

            val persisted = when {
                current == null -> null
                photoStorage.isTemporary(current) -> photoStorage.persist(current)
                // Фотографию не меняли — она уже лежит в хранилище под прежним именем
                else -> savedPhotoName
            }
            if (current != null && persisted == null) {
                // Перенести файл не удалось — оставляем метке прежнюю фотографию
                showToast(getString(R.string.photo_save_failed))
                return savedPhotoName
            }

            // Прежнее фото метки больше не используется — освобождаем место
            if (savedPhotoName != null && savedPhotoName != persisted) {
                photoStorage.deletePhoto(savedPhotoName)
            }
            return persisted
        }

        /** Диалог закрыли без сохранения: снятый или выбранный файл остаётся мусором */
        fun discardUncommittedPhoto() {
            val current = photoPath
            if (!isCommitted && current != null && photoStorage.isTemporary(current)) {
                photoStorage.deleteFile(current)
                photoPath = null
            }
        }

        private fun bindPhoto() {
            val path = photoPath
            val bitmap = path?.let { photoStorage.decodeScaled(it, PHOTO_PREVIEW_MAX_SIZE_PX) }

            if (bitmap == null) {
                // Файл могли удалить извне — тогда считаем, что фотографии у метки нет
                photoPath = null
                photoImageView.setImageDrawable(null)
                photoImageView.visibility = View.GONE
                photoHintTextView.setText(R.string.attraction_photo_empty)
                addPhotoButton.setText(R.string.action_add_photo)
                removePhotoButton.visibility = View.GONE
            } else {
                photoImageView.setImageBitmap(bitmap)
                photoImageView.visibility = View.VISIBLE
                photoHintTextView.setText(R.string.attraction_photo_hint)
                addPhotoButton.setText(R.string.action_change_photo)
                removePhotoButton.visibility = View.VISIBLE
            }
        }
    }

    // Источник фотографии: камера или галерея
    private fun showPhotoSourceDialog() {
        val options = arrayOf(
            getString(R.string.photo_source_camera),
            getString(R.string.photo_source_gallery)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.photo_source_dialog_title)
            .setItems(options) { _, which ->
                if (which == 0) {
                    requestPhotoFromCamera()
                } else {
                    // Системный выбор изображения работает без разрешения на чтение хранилища
                    pickPhotoLauncher.launch("image/*")
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    // Разрешение CAMERA объявлено в манифесте, поэтому без него камеру открыть нельзя
    private fun requestPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        if (activePhotoController == null) return

        val output = try {
            photoStorage.createCameraOutput()
        } catch (e: IllegalArgumentException) {
            Log.e("MapFragment", "Failed to prepare camera output", e)
            showToast(getString(R.string.photo_save_failed))
            return
        }

        pendingCameraPhotoPath = output.path
        try {
            takePhotoLauncher.launch(output.uri)
        } catch (e: ActivityNotFoundException) {
            // Приложения камеры на устройстве нет — остаётся галерея
            Log.e("MapFragment", "No camera app available", e)
            pendingCameraPhotoPath = null
            photoStorage.deleteFile(output.path)
            showToast(getString(R.string.camera_unavailable))
        }
    }

    // Фотография метки во весь экран
    private fun showPhotoPreviewDialog(path: String) {
        val bitmap = photoStorage.decodeScaled(path, PHOTO_FULL_MAX_SIZE_PX)
        if (bitmap == null) {
            showToast(getString(R.string.photo_load_failed))
            return
        }

        val previewView = layoutInflater.inflate(R.layout.dialog_photo_preview, null)
        previewView.findViewById<ImageView>(R.id.previewImageView).setImageBitmap(bitmap)
        AlertDialog.Builder(requireContext())
            .setView(previewView)
            .setPositiveButton(R.string.action_close, null)
            .show()
    }

    /**
     * Отправляет метку в другое приложение: текстом уходят название, описание, координаты
     * и ссылка на карту, а при наличии фотографии к ним прикладывается и она.
     */
    private fun shareAttraction(
        name: String,
        description: String,
        position: GeoPoint,
        photoPath: String?
    ) {
        // Фото могли удалить извне — тогда делимся одним текстом
        val photoUri = photoPath?.let { photoStorage.shareUri(it) }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, name)
            putExtra(Intent.EXTRA_TEXT, buildShareText(name, description, position))
            if (photoUri == null) {
                type = "text/plain"
            } else {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, photoUri)
                // Наши файлы лежат в личной папке: без этого флага получатель их не прочитает
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        try {
            startActivity(
                Intent.createChooser(shareIntent, getString(R.string.share_attraction_chooser_title))
            )
        } catch (e: ActivityNotFoundException) {
            Log.e("MapFragment", "No app to share attraction with", e)
            showToast(getString(R.string.share_no_app))
        }
    }

    // Координаты пишем тем же форматом, что и в полях диалога, и дублируем ссылкой на карту
    private fun buildShareText(name: String, description: String, position: GeoPoint): String {
        val latitude = formatCoordinate(position.latitude)
        val longitude = formatCoordinate(position.longitude)

        return listOf(
            name,
            description,
            getString(R.string.share_attraction_coordinates, latitude, longitude),
            getString(R.string.share_attraction_map_link, latitude, longitude)
        ).filter { it.isNotBlank() }.joinToString("\n")
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
        // Ссылки на снятые с карты метки поиску уже ни к чему
        markersByAttractionId.clear()

        attractions.forEach { attraction ->
            Log.d("MapFragment", "Adding marker at ${attraction.latitude}, ${attraction.longitude} for ${attraction.name}")
            val marker = Marker(mapView).apply {
                position = GeoPoint(attraction.latitude, attraction.longitude)
                title = attraction.name
                snippet = buildMarkerSnippet(attraction)
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
            markersByAttractionId[attraction.id] = marker
        }

        mapView.invalidate()
    }

    // У метки с фотографией в подписи отмечаем, что фото можно открыть в её диалоге
    private fun buildMarkerSnippet(attraction: Attraction): String {
        if (!photoStorage.exists(attraction.photoName)) return attraction.description

        return listOf(attraction.description, getString(R.string.attraction_photo_attached))
            .filter { it.isNotBlank() }
            .joinToString("\n")
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
        // Фрагмент уходит вместе с открытым диалогом — незакреплённое фото удаляем.
        // Файл незавершённого снимка не трогаем: камера может писать в него прямо сейчас
        activePhotoController?.discardUncommittedPhoto()
        activePhotoController = null

        markersByAttractionId.clear()
        locationOverlay.disableMyLocation()
        mapView.onDetach()
        super.onDestroyView()
        _binding = null // Очищаем binding, чтобы избежать утечек памяти
    }
}
