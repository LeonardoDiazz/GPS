package mx.edu.utez.gps.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
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
class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TripRepository(application)
    private val locationClient = LocationClient(
        application,
        LocationServices.getFusedLocationProviderClient(application)
    )
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState = _uiState.asStateFlow()
    private var locationJob: Job? = null
    fun onStartStopClick() {
        if (_uiState.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }
    private fun startRecording() {
        locationJob?.cancel() // Cancela cualquier trabajo anterior
        viewModelScope.launch {
// 1. Crear un nuevo viaje en la BD
            val newTripId = repository.startNewTrip()
// 2. Actualizar el estado de la UI
            _uiState.update { TrackingUiState(isRecording = true, currentTripId = newTripId) }
// 3. Empezar a escuchar el GPS
            locationJob = locationClient.getLocationUpdates(5000L) // cada 5 seg
                .catch { e -> e.printStackTrace() } // Manejar error
                .onEach { location ->
// 4. Guardar cada punto en la BD
                    val point = LocationPoint(
                        tripId = newTripId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveLocationPoint(point)
                }
                .launchIn(viewModelScope) // Lanza la corutina
        }
    }
    private fun stopRecording() {
        locationJob?.cancel()
        _uiState.update { it.copy(isRecording = false) }
    }
    // Se llama DESPUÉS de que la cámara toma la foto
    fun savePhotoAndUpdateTrip(photoUri: Uri) {
        val tripId = _uiState.value.currentTripId ?: return
        viewModelScope.launch {
            repository.stopTripAndAddPhoto(tripId, photoUri.toString())
// Reseteamos el estado para el próximo viaje
            _uiState.update { TrackingUiState(isRecording = false, currentTripId = null) }
        }
    }
}