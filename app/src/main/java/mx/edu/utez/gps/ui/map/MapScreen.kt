package mx.edu.utez.gps.ui.map

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.gps.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val allPoints by viewModel.allTripPoints.collectAsState()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Inicializar configuración OSMdroid
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osmdroid", 0)
            )

            // Crear el MapView
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                // Si tienes coordenadas, centra el mapa
                allPoints.firstOrNull()?.let {
                    controller.setCenter(GeoPoint(it.latitude, it.longitude))
                }
            }
        },
        update = { mapView ->
            // Actualizar posición si cambian los puntos
            allPoints.firstOrNull()?.let {
                mapView.controller.setCenter(GeoPoint(it.latitude, it.longitude))
            }
        }
    )
}
