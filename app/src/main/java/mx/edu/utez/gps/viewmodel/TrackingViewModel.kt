package mx.edu.utez.gps.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import mx.edu.utez.gps.data.db.LocationPoint
import mx.edu.utez.gps.data.location.LocationClient
import mx.edu.utez.gps.data.repository.TripRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackingUiState(
    val isRecording: Boolean = false,
    val currentTripId: Long? = null
)

// AGREGAMOS savedStateHandle AL CONSTRUCTOR
class TrackingViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = TripRepository(application)
    private val locationClient = LocationClient(
        application,
        LocationServices.getFusedLocationProviderClient(application)
    )

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null

    // --- INIT: VERIFICAR SI VENIMOS DE "CONTINUAR RUTA" ---
    init {
        val navTripId = savedStateHandle.get<Long>("tripId") ?: -1L
        if (navTripId != -1L) {
            // Si hay un ID válido, iniciamos en modo RESUMEN automáticamente
            resumeRecording(navTripId)
        }
    }

    fun onStartStopClick() {
        if (_uiState.value.isRecording) stopRecording()
        else startRecording()
    }

    // --- NUEVO VIAJE (Desde cero) ---
    private fun startRecording() {
        if (locationJob != null && locationJob?.isActive == true) return
        locationJob?.cancel()

        viewModelScope.launch {
            val newTripId = repository.startNewTrip()
            if (newTripId <= 0) return@launch

            startLocationUpdates(newTripId)
        }
    }

    // --- CONTINUAR VIAJE (Desde Galería) ---
    private fun resumeRecording(existingTripId: Long) {
        if (locationJob != null && locationJob?.isActive == true) return
        locationJob?.cancel()

        viewModelScope.launch {
            // 1. Reabrimos el viaje en BD (endTime = null)
            repository.resumeTrip(existingTripId)

            // 2. Iniciamos actualizaciones de GPS sobre ese ID existente
            startLocationUpdates(existingTripId)
        }
    }

    // Helper para iniciar el GPS (común para nuevo y continuar)
    private fun startLocationUpdates(tripId: Long) {
        _uiState.update {
            it.copy(isRecording = true, currentTripId = tripId)
        }

        locationJob = locationClient.getLocationUpdates(5000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val point = LocationPoint(
                    tripId = tripId, // Usamos el ID (sea nuevo o viejo)
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveLocationPoint(point)
            }
            .launchIn(viewModelScope)
    }

    private fun stopRecording() {
        locationJob?.cancel()
        locationJob = null
        _uiState.update {
            it.copy(isRecording = false)
        }
    }

    fun finalizeTrip(photoUri: Uri, title: String) {
        val tripId = _uiState.value.currentTripId ?: return
        viewModelScope.launch {
            // Al finalizar, repository calculará la distancia TOTAL (puntos viejos + nuevos)
            repository.stopTripAndSaveDetails(tripId, photoUri.toString(), title)
            _uiState.update {
                TrackingUiState(isRecording = false, currentTripId = null)
            }
        }
    }
}