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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.techevent.data.AppDatabase
import com.example.techevent.data.EventRepository
import com.example.techevent.data.RetrofitClient
import com.example.techevent.data.ThemePreferences
import com.example.techevent.ui.EventViewModel
import com.example.techevent.ui.EventViewModelFactory
import com.example.techevent.ui.MainNavigationContainer
import com.example.theme.TechEventTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val apiService = RetrofitClient.apiService
        val repository = EventRepository(database.eventoDao(), apiService)
        val themePreferences = ThemePreferences(this)

        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val isDarkMode by themePreferences.esModoOscuro.collectAsState(initial = false)

            val viewModel: EventViewModel = viewModel(
                factory = EventViewModelFactory(repository)
            )

            val windowSize = calculateWindowSizeClass(this)
            val esTablet = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded

            TechEventTheme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainNavigationContainer(
                        viewModel = viewModel,
                        esTablet = esTablet,
                        isDarkMode = isDarkMode,
                        onToggleTheme = { nuevoValor ->
                            scope.launch { themePreferences.guardarModoOscuro(nuevoValor) }
                        }
                    )
                }
            }
        }
    }
}