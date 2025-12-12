package mx.edu.utez.gps.ui.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.gps.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    // Obtenemos el mapa de viajes agrupados
    val tripsData by viewModel.tripsData.collectAsState()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osmdroid", 0)
            )

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            // Limpiamos el mapa antes de redibujar
            mapView.overlays.clear()

            // Iteramos sobre cada viaje (entry.key es el ID, entry.value son los puntos)
            tripsData.forEach { (tripId, points) ->

                if (points.isNotEmpty()) {
                    val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }

                    // --- 1. Dibujar LA LÍNEA DEL VIAJE (Polilínea) ---
                    val line = Polyline().apply {
                        setPoints(geoPoints)
                        outlinePaint.color = Color.BLUE // Puedes cambiar el color o hacerlo aleatorio
                        outlinePaint.strokeWidth = 10f

                        // Opcional: Permitir click en la línea para ver info
                        title = "Viaje #$tripId"
                    }
                    mapView.overlays.add(line)

                    // --- 2. MARCADOR DE INICIO (Verde) ---
                    val startMarker = Marker(mapView).apply {
                        position = geoPoints.first()
                        title = "Inicio Viaje #$tripId"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        // Si quieres cambiar el icono a uno verde, se hace aquí,
                        // pero por defecto usa el de osmdroid.
                    }
                    mapView.overlays.add(startMarker)

                    // --- 3. MARCADOR DE FIN (Rojo) ---
                    // Solo lo ponemos si hay más de 1 punto y es distinto al inicio
                    if (geoPoints.size > 1) {
                        val endMarker = Marker(mapView).apply {
                            position = geoPoints.last()
                            title = "Fin Viaje #$tripId"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(endMarker)
                    }
                }
            }

            // Centrar el mapa en el último viaje registrado (si existe)
            // Esto es un extra para que la cámara no se quede en el océano
            if (tripsData.isNotEmpty()) {
                // Buscamos el ID más alto (el último viaje)
                val lastTripPoints = tripsData.maxByOrNull { it.key }?.value
                lastTripPoints?.lastOrNull()?.let { lastPoint ->
                    mapView.controller.setCenter(GeoPoint(lastPoint.latitude, lastPoint.longitude))
                }
            }

            mapView.invalidate()
        }
    )
}