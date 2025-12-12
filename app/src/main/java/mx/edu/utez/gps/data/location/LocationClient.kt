        package mx.edu.utez.gps.data.location

        import android.annotation.SuppressLint
        import android.content.Context
        import android.os.Looper
        import com.google.android.gms.location.FusedLocationProviderClient
        import com.google.android.gms.location.LocationCallback
        import com.google.android.gms.location.LocationRequest
        import com.google.android.gms.location.LocationResult
        import com.google.android.gms.location.Priority
        import kotlinx.coroutines.channels.awaitClose
        import kotlinx.coroutines.flow.Flow
        import kotlinx.coroutines.flow.callbackFlow
        class LocationClient(
            private val context: Context,
            private val client: FusedLocationProviderClient
        ) {
            @SuppressLint("MissingPermission") // Asumimos que los permisos se piden en la UI
            fun getLocationUpdates(interval: Long): Flow<android.location.Location> {
        // callbackFlow es la magia para convertir callbacks en Flows
                return callbackFlow {
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, interval
                    ).build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            super.onLocationResult(result)
                            result.locations.lastOrNull()?.let { location ->
                                trySend(location) // Envía la ubicación al Flow
                            }
                        }
                    }
                    client.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
        // Esto se llama cuando el Flow se cancela (ej: el ViewModel se destruye)
                    awaitClose {
                        client.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }