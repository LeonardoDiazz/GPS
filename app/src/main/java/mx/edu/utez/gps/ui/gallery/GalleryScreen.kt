package mx.edu.utez.gps.ui.gallery

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mx.edu.utez.gps.data.db.Trip
import mx.edu.utez.gps.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = viewModel()
) {
    val trips by viewModel.completedTrips.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado de la pantalla
        Text(
            text = "Mis Rutas (Nube)",
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
                        viewModel.deleteTrip(it)
                    },
                    onContinue = {
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
    // Estado para controlar si la tarjeta está expandida
    var expanded by remember { mutableStateOf(false) }

    // --- CAMBIO IMPORTANTE: Decodificar Base64 a Bitmap ---
    // Usamos remember para hacer la conversión solo una vez cuando cambia la URI
    val imageBitmap = remember(trip.photoUri) {
        base64ToBitmap(trip.photoUri)
    }

    val durationMillis = (trip.endTime ?: 0L) - trip.startTime
    val durationStr = formatDuration(durationMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // --- 1. ENCABEZADO ---
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
                Icon(
                    imageVector = if(expanded) Icons.Default.PlayArrow else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // --- 2. FOTO (Decodificada desde Base64) ---
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Foto del viaje",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback si no hay imagen o falló la conversión
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin Imagen", color = Color.White)
                }
            }

            // --- 3. DATOS ---
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha:",
                    value = trip.endTime?.toFormattedDate() ?: "En curso..."
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

                InfoRow(
                    icon = Icons.Default.Straighten,
                    label = "Distancia:",
                    value = "%.2f metros".format(trip.distance)
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(
                    icon = Icons.Default.Timer,
                    label = "Duración:",
                    value = durationStr
                )

                // --- 4. BOTONES ---
                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
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

// --- NUEVA FUNCIÓN: CONVERTIR TEXTO BASE64 A IMAGEN ---
fun base64ToBitmap(base64Str: String?): androidx.compose.ui.graphics.ImageBitmap? {
    if (base64Str.isNullOrEmpty()) return null
    return try {
        // Si el string viene con el prefijo "data:image/jpeg;base64,", lo limpiamos
        val pureBase64 = if (base64Str.contains(",")) {
            base64Str.substringAfter(",")
        } else {
            base64Str
        }

        // Decodificamos bytes
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        // Creamos Bitmap
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        // Convertimos a ImageBitmap (formato de Compose)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// --- HELPERS DE FORMATO ---
fun Long.toFormattedDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d min %02d seg", minutes, seconds)
}