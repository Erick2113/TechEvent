package com.example.techevent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.techevent.data.AppDatabase
import com.example.techevent.data.EventRepository
import com.example.techevent.data.RetrofitClient
import com.example.techevent.data.ThemePreferences
import com.example.techevent.ui.EventViewModel
import com.example.techevent.ui.EventViewModelFactory
import com.example.techevent.ui.MainNavigationContainer
import com.example.theme.TechEventTheme
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instanciar la Base de Datos y la API
        val database = AppDatabase.getDatabase(this)
        val apiService = RetrofitClient.apiService
        val repository = EventRepository(database.eventoDao(), apiService)

        // 2. Instanciar DataStore para el tema
        val themePreferences = ThemePreferences(this)

        enableEdgeToEdge()
        setContent {
            // Recolectar el estado del tema
            val isDarkMode by themePreferences.esModoOscuro.collectAsState(initial = false)

            // Instanciar el ViewModel
            val viewModel: EventViewModel = viewModel(
                factory = EventViewModelFactory(repository)
            )

            // Detectar si es Tablet
            val windowSize = calculateWindowSizeClass(this)
            val esTablet = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded

            // Aplicar el tema
            TechEventTheme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // AQUÍ ESTABA EL ERROR: Solo necesita el viewModel y el esTablet
                    MainNavigationContainer(
                        viewModel = viewModel,
                        esTablet = esTablet
                    )
                }
            }
        }
    }
}