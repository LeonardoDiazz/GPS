package mx.edu.utez.gps.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface TripDao {
    // Inserta un nuevo viaje y devuelve su ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun startNewTrip(trip: Trip): Long
    // Actualiza un viaje (para añadir la foto y la hora de fin)
    @Update
    suspend fun updateTrip(trip: Trip)
    // Inserta un punto GPS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(point: LocationPoint)
    // Obtiene todos los puntos de todos los viajes (para el mapa)
    @Query("SELECT * FROM location_points ORDER BY timestamp ASC")
    fun getAllLocationPoints(): Flow<List<LocationPoint>>
// Obtiene todos los viajes terminados (para la galería)
@Query("SELECT * FROM trips WHERE photoUri IS NOT NULL ORDER BY endTime DESC")
fun getAllCompletedTrips(): Flow<List<Trip>>
    // Obtiene un viaje por su ID (para actualizarlo)
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?
}