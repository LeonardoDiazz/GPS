package mx.edu.utez.gps.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
@Entity(
    tableName = "location_points",
    primaryKeys = ["tripId", "timestamp"], // Clave única
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE // Si borras un Trip, se borran sus puntos
        )
    ]
)
data class LocationPoint(
    val tripId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)