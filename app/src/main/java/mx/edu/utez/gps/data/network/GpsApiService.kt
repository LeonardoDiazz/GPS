package mx.edu.utez.gps.data.network


import mx.edu.utez.gps.data.db.LocationPoint
import mx.edu.utez.gps.data.db.Trip
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GpsApiService {
    // Obtener todos los viajes
    @GET("trips")
    suspend fun getAllTrips(): List<Trip>

    // Crear viaje nuevo
    @POST("trips")
    suspend fun createTrip(@Body trip: Trip): Trip

    // Enviar un punto GPS
    @POST("points")
    suspend fun sendLocationPoint(@Body point: LocationPoint)

    // Obtener puntos de un viaje (Filtro de MockAPI)
    @GET("points")
    suspend fun getPointsByTrip(@Query("tripId") tripId: Long): List<LocationPoint>

    // Obtener TODOS los puntos (Para el mapa general)
    @GET("points")
    suspend fun getAllPoints(): List<LocationPoint>

    // Actualizar viaje (finalizar)
    @PUT("trips/{id}")
    suspend fun updateTrip(@Path("id") id: Long, @Body trip: Trip)

    // Eliminar viaje
    @DELETE("trips/{id}")
    suspend fun deleteTrip(@Path("id") id: Long)
}