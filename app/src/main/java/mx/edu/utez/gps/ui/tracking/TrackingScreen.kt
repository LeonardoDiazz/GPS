package mx.edu.utez.gps.ui.tracking

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.gps.viewmodel.TrackingViewModel
import java.io.File

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- ESTADOS ---
    // 1. Usamos remember para que la URI no se pierda al rotar pantalla o volver de la cámara
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // 2. Estados para controlar el Diálogo de Título
    var showTitleDialog by remember { mutableStateOf(false) }
    var tripTitle by remember { mutableStateOf("") }

    // --- Lógica de Cámara ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            // Se llama cuando la cámara termina
            if (success && tempPhotoUri != null) {
                // CAMBIO: En lugar de guardar directo, mostramos el diálogo
                showTitleDialog = true
            }
        }
    )

    // --- Lógica de Permisos ---
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                // Opcional: Mostrar mensaje de error
            }
        }
    )

    // Pedir permisos al inicio
    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(permissionsToRequest)
    }

    // --- UI PRINCIPAL ---
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Indicador de estado
            Text(
                text = if (uiState.isRecording) "GRABANDO RECORRIDO..." else "LISTO PARA INICIAR",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    if (uiState.isRecording) {
                        // 1. Si está grabando, paramos el GPS (pero no cerramos el viaje aún)
                        viewModel.onStartStopClick()

                        // 2. Generamos URI, guardamos en state y lanzamos cámara
                        val uri = generateTempUri(context)
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        // Si no está grabando, empezamos
                        viewModel.onStartStopClick()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text(if (uiState.isRecording) "Detener y Foto" else "Iniciar Grabación")
            }

            if (uiState.isRecording) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Text(
                    text = "Viaje ID: ${uiState.currentTripId}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // --- CUADRO DE DIÁLOGO (ALERT DIALOG) ---
        if (showTitleDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Opcional: Qué hacer si cancelan (forzar guardado sin título o no hacer nada)
                },
                title = { Text(text = "Guardar Recorrido") },
                text = {
                    Column {
                        Text("Asigna un nombre a tu viaje para identificarlo:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tripTitle,
                            onValueChange = { tripTitle = it },
                            label = { Text("Ej: Ruta al parque") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Validar título vacío
                            val finalTitle = if (tripTitle.isBlank()) "Sin Título" else tripTitle

                            // Llamar al ViewModel con URI y Título
                            viewModel.finalizeTrip(tempPhotoUri!!, finalTitle)

                            // Limpiar estados
                            showTitleDialog = false
                            tripTitle = ""
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    // Botón opcional para cancelar (aunque ya se detuvo el GPS)
                }
            )
        }
    }
}

// Función helper para crear una URI donde guardar la foto
private fun generateTempUri(context: Context): Uri {
    val file = File(context.filesDir, "trip_photo_${System.currentTimeMillis()}.jpg")
    // Asegúrate de que esto coincida con tu AndroidManifest -> provider authorities
    val authority = "${context.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, file)
}