package mx.edu.utez.gps.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var photoUri: String? = null,
    var title: String = "Sin Título",

    // --- NUEVO CAMPO PARA EL PUNTO 2 ---
    // Guardará la distancia total calculada en metros
    var distance: Double = 0.0
)