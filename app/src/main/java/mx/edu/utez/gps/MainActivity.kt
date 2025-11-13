package mx.edu.utez.gps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import mx.edu.utez.gps.ui.AppNavigation
import mx.edu.utez.gps.ui.theme.GPSTheme // Asegúrate de usar tu tema
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSTheme {
                AppNavigation()
            }
        }
    }
}