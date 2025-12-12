package mx.edu.utez.gps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import mx.edu.utez.gps.data.db.LocationPoint
import mx.edu.utez.gps.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TripRepository(application)

    // --- CAMBIO IMPORTANTE ---
    // En lugar de una lista plana, transformamos los datos a un Mapa agrupado por ID de viaje.
    // Tipo: Map<Long, List<LocationPoint>>
    val tripsData: StateFlow<Map<Long, List<LocationPoint>>> =
        repository.getAllPoints()
            .map { points ->
                // Esta función mágica agrupa los puntos según su tripId
                points.groupBy { it.tripId }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap()
            )
}