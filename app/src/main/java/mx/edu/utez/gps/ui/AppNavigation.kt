package mx.edu.utez.gps.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utez.gps.ui.gallery.GalleryScreen
import mx.edu.utez.gps.ui.map.MapScreen
import mx.edu.utez.gps.ui.tracking.TrackingScreen

// 1. Definimos las rutas de las pestañas
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Tracking : Screen("tracking", "Grabar", Icons.Default.LocationOn)
    object Map : Screen("map", "Mapa", Icons.Default.Map)
    object Gallery : Screen("gallery", "Mis rutas", Icons.Default.List)
}

val bottomNavItems = listOf(
    Screen.Tracking,
    Screen.Map,
    Screen.Gallery
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 2. Scaffold nos da la estructura (con barra inferior)
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // --- CORRECCIÓN DEL ERROR ---
                                // En lugar de usar findStartDestination() que falla con argumentos,
                                // usamos explícitamente la ruta patrón de la pantalla de inicio.
                                popUpTo("${Screen.Tracking.route}?tripId={tripId}") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 3. NavHost es el contenedor que cambia las pantallas
        NavHost(
            navController = navController,
            startDestination = Screen.Tracking.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Ruta de Tracking con argumentos
            composable(
                route = "${Screen.Tracking.route}?tripId={tripId}",
                arguments = listOf(
                    navArgument("tripId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                TrackingScreen()
            }

            composable(Screen.Map.route) { MapScreen() }

            composable(Screen.Gallery.route) {
                GalleryScreen(navController = navController)
            }
        }
    }
}