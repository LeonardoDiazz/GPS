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


    // -----------------------
    //  BOTÓN INICIAR / DETENER
    // -----------------------
    fun onStartStopClick() {
        if (_uiState.value.isRecording) stopRecording()
        else startRecording()
    }


    // -----------------------
    //  INICIAR GRABACIÓN
    // -----------------------
    private fun startRecording() {

        // Evita doble clics rápidos
        if (locationJob != null && locationJob?.isActive == true) return

        locationJob?.cancel()

        viewModelScope.launch {

            // 1. Crear Trip
            val newTripId = repository.startNewTrip()
            if (newTripId <= 0) return@launch

            // 2. Guardar en el estado
            _uiState.update {
                it.copy(isRecording = true, currentTripId = newTripId)
            }

            // 3. Escuchar ubicación
            locationJob = locationClient.getLocationUpdates(5000L)
                .catch { e -> e.printStackTrace() }
                .onEach { location ->

                    val point = LocationPoint(
                        tripId = newTripId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )

                    repository.saveLocationPoint(point)
                }
                .launchIn(viewModelScope)
        }
    }


    // -----------------------
    //  DETENER GRABACIÓN
    // -----------------------
    private fun stopRecording() {

        val tripId = _uiState.value.currentTripId

        locationJob?.cancel()
        locationJob = null

        // Si quieres que TODO viaje tenga endTime aunque no haya foto:
        if (tripId != null) {
            viewModelScope.launch {
                repository.stopTripAndAddPhoto(tripId, "") // sin foto
            }
        }

        _uiState.update {
            it.copy(isRecording = false)
        }
    }


    // -----------------------
    //  GUARDAR FOTO Y CERRAR VIAJE
    // -----------------------
    fun savePhotoAndUpdateTrip(photoUri: Uri) {

        val tripId = _uiState.value.currentTripId ?: return

        viewModelScope.launch {
            repository.stopTripAndAddPhoto(tripId, photoUri.toString())

            // Reset del estado
            _uiState.update {
                TrackingUiState(isRecording = false, currentTripId = null)
            }
        }
    }
}
