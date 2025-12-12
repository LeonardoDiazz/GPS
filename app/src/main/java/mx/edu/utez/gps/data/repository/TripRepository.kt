package mx.edu.utez.gps.data.repository

import android.content.Context
import android.location.Location
import mx.edu.utez.gps.data.db.AppDatabase
import mx.edu.utez.gps.data.db.LocationPoint
import mx.edu.utez.gps.data.db.Trip

class TripRepository(context: Context) {
    private val tripDao = AppDatabase.getDatabase(context).tripDao()

    fun getAllPoints() = tripDao.getAllLocationPoints()
    fun getAllCompletedTrips() = tripDao.getAllCompletedTrips()

    suspend fun startNewTrip(): Long {
        return tripDao.startNewTrip(Trip())
    }

    suspend fun saveLocationPoint(point: LocationPoint) {
        tripDao.insertLocationPoint(point)
    }

    suspend fun deleteTrip(tripId: Long) {
        tripDao.deleteTrip(tripId)
    }

    // --- NUEVO: Función para reanudar ---
    suspend fun resumeTrip(tripId: Long) {
        tripDao.resumeTrip(tripId)
    }

    suspend fun stopTripAndSaveDetails(tripId: Long, photoUri: String, title: String) {
        val trip = tripDao.getTripById(tripId)
        val points = tripDao.getPointsForTrip(tripId)
        val totalDistance = calculateDistance(points)

        trip?.let {
            it.endTime = System.currentTimeMillis()
            it.photoUri = photoUri
            it.title = title
            it.distance = totalDistance
            tripDao.updateTrip(it)
        }
    }

    private fun calculateDistance(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0
        var totalDistance = 0.0
        val results = FloatArray(1)
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
            totalDistance += results[0]
        }
        return totalDistance
    }

    suspend fun stopTripAndAddPhoto(tripId: Long, photoUri: String) {
        // Función Legacy, se mantiene por si acaso
        val trip = tripDao.getTripById(tripId)
        trip?.let {
            it.endTime = System.currentTimeMillis()
            it.photoUri = photoUri
            tripDao.updateTrip(it)
        }
    }
}