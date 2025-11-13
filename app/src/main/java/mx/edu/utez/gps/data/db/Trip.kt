package mx.edu.utez.gps.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var photoUri: String? = null // Guardamos la URI de la foto como String
)