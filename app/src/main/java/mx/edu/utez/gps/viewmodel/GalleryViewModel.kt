package mx.edu.utez.gps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import mx.edu.utez.gps.data.db.Trip
import mx.edu.utez.gps.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TripRepository(application)

    // Flujo de datos que observa la base de datos
    val completedTrips: StateFlow<List<Trip>> =
        repository.getAllCompletedTrips()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // --- NUEVA FUNCIÓN PARA ELIMINAR ---
    // Esta función será llamada desde la UI cuando se presione "Eliminar"
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTrip(trip.id)
        }
    }
}