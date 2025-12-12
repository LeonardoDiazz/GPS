package mx.edu.utez.gps.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mx.edu.utez.gps.data.db.LocationPoint
import mx.edu.utez.gps.data.db.Trip
import mx.edu.utez.gps.data.network.RetrofitClient
import java.io.ByteArrayOutputStream

class TripRepository(private val context: Context) {

    // CAMBIO 1: Usamos la API (Retrofit) en lugar del DAO (Room)
    private val api = RetrofitClient.apiService

    // --- 1. OBTENER VIAJES (Flow desde la Nube) ---
    fun getAllCompletedTrips(): Flow<List<Trip>> = flow {
        try {
            val trips = api.getAllTrips()
            // Filtramos solo los que ya terminaron (tienen fecha fin)
            val completed = trips.filter { it.endTime != null }
            emit(completed)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList()) // Si falla internet, devolvemos lista vacía
        }
    }

    // --- 2. OBTENER PUNTOS (Para el mapa) ---
    fun getAllPoints(): Flow<List<LocationPoint>> = flow {
        try {
            // Nota: Pedir TODOS los puntos puede ser lento, pero mantiene tu lógica actual
            val points = api.getAllPoints()
            emit(points)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // --- 3. INICIAR VIAJE ---
    suspend fun startNewTrip(): Long {
        return try {
            val trip = Trip(startTime = System.currentTimeMillis())
            // MockAPI crea el ID y nos lo devuelve
            val response = api.createTrip(trip)
            response.id
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    // --- 4. GUARDAR PUNTO GPS ---
    suspend fun saveLocationPoint(point: LocationPoint) {
        try {
            api.sendLocationPoint(point)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- 5. ELIMINAR VIAJE ---
    suspend fun deleteTrip(tripId: Long) {
        try {
            api.deleteTrip(tripId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- 6. REANUDAR VIAJE ---
    suspend fun resumeTrip(tripId: Long) {
        // En MockAPI, simplemente seguimos enviando puntos con este ID.
        // No es estrictamente necesario actualizar el campo endTime a null en la nube
        // para que funcione, así que lo dejamos vacío para ahorrar datos.
    }

    // --- 7. FINALIZAR VIAJE (Cálculo de distancia y Foto Base64) ---
    suspend fun stopTripAndSaveDetails(tripId: Long, photoUriStr: String, title: String) {
        try {
            // A. Pedimos los puntos a la nube para calcular la distancia
            val points = api.getPointsByTrip(tripId)
            val totalDistance = calculateDistance(points)

            // B. Convertimos la foto (Uri) a Texto (Base64) para MockAPI
            val base64Image = encodeImageToBase64(Uri.parse(photoUriStr))

            // C. Creamos el objeto actualizado
            val updatedTrip = Trip(
                id = tripId,
                startTime = points.firstOrNull()?.timestamp ?: System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                photoUri = base64Image, // Enviamos el texto de la imagen
                title = title,
                distance = totalDistance
            )

            // D. Enviamos la actualización al servidor
            api.updateTrip(tripId, updatedTrip)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- UTILERÍAS ---

    // Misma lógica matemática, pero trabajando con datos de la nube
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

    // FUNCION NUEVA: Convierte la imagen a String (Base64)
    private fun encodeImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Reducimos la imagen a 400x400 para no saturar MockAPI
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)

            val outputStream = ByteArrayOutputStream()
            // Comprimimos a JPEG calidad 50
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            // Retornamos el string listo para HTML/Web
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Función Legacy (ya no se usa mucho, pero la mantenemos para que no rompa tu código anterior)
    suspend fun stopTripAndAddPhoto(tripId: Long, photoUri: String) {
        // Redirigimos a la función nueva con título vacío si se llega a llamar
        stopTripAndSaveDetails(tripId, photoUri, "Sin Título")
    }
}