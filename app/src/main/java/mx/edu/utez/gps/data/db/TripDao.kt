package mx.edu.utez.gps.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun startNewTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(point: LocationPoint)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    // --- NUEVO: REABRIR EL VIAJE ---
    // Ponemos endTime en NULL para indicar que está activo de nuevo
    @Query("UPDATE trips SET endTime = NULL WHERE id = :tripId")
    suspend fun resumeTrip(tripId: Long)

    @Query("SELECT * FROM location_points ORDER BY timestamp ASC")
    fun getAllLocationPoints(): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getPointsForTrip(tripId: Long): List<LocationPoint>

    @Query("SELECT * FROM trips WHERE photoUri IS NOT NULL ORDER BY endTime DESC")
    fun getAllCompletedTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?
}