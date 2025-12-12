package mx.edu.utez.gps.ui.gallery

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import mx.edu.utez.gps.data.db.Trip
import mx.edu.utez.gps.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun GalleryScreen(
    navController: NavController, // Necesario para navegar enviando argumentos
    viewModel: GalleryViewModel = viewModel()
) {
    val trips by viewModel.completedTrips.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado de la pantalla
        Text(
            text = "Mis Rutas",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trips) { trip ->
                TripInteractiveCard(
                    trip = trip,
                    onDelete = {
                        // Llamamos a la función de borrar del ViewModel
                        viewModel.deleteTrip(it)
                    },
                    onContinue = {
                        // --- CAMBIO IMPORTANTE AQUÍ ---
                        // Navegamos a la pantalla de grabación pasando el ID del viaje actual.
                        // Esto activa la lógica de "Reanudar" en el TrackingViewModel.
                        navController.navigate("tracking?tripId=${trip.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun TripInteractiveCard(
    trip: Trip,
    onDelete: (Trip) -> Unit,
    onContinue: () -> Unit
) {
    // Estado para controlar si la tarjeta está expandida o cerrada
    var expanded by remember { mutableStateOf(false) }

    // Cálculo de duración (Fin - Inicio)
    val durationMillis = (trip.endTime ?: 0L) - trip.startTime
    val durationStr = formatDuration(durationMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Animación suave al abrir/cerrar
            .clickable { expanded = !expanded }, // Click para expandir
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // --- 1. ENCABEZADO: Título e ID ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Icono visual
                Icon(
                    imageVector = if(expanded) Icons.Default.PlayArrow else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // --- 2. FOTO ---
            AsyncImage(
                model = trip.photoUri?.let { Uri.parse(it) },
                contentDescription = "Foto del viaje",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // --- 3. DATOS DEL RECORRIDO ---
            Column(modifier = Modifier.padding(16.dp)) {

                // Fecha
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha:",
                    value = trip.endTime?.toFormattedDate() ?: "En curso..."
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

                // Distancia (Recorrido)
                InfoRow(
                    icon = Icons.Default.Straighten,
                    label = "Distancia:",
                    value = "%.2f metros".format(trip.distance)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tiempo (Duración)
                InfoRow(
                    icon = Icons.Default.Timer,
                    label = "Duración:",
                    value = durationStr
                )

                // --- 4. BOTONES (SOLO VISIBLES SI ESTÁ EXPANDIDO) ---
                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Botón ELIMINAR RUTA
                        OutlinedButton(
                            onClick = { onDelete(trip) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eliminar ruta")
                        }

                        // Botón CONTINUAR RUTA
                        Button(
                            onClick = { onContinue() }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continuar ruta")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// --- HELPERS ---

// Formato de fecha
fun Long.toFormattedDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

// Formato de duración
fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d min %02d seg", minutes, seconds)
}