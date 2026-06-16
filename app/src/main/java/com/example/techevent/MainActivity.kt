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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.techevent.data.AppDatabase
import com.example.techevent.data.EventRepository
import com.example.techevent.data.RetrofitClient
import com.example.techevent.data.SessionPreferences
import com.example.techevent.data.ThemePreferences
import com.example.techevent.ui.EventViewModel
import com.example.techevent.ui.EventViewModelFactory
import com.example.techevent.ui.LoginViewModel
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
        val sessionPreferences = SessionPreferences(this)

        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()


            val isDarkMode by themePreferences.esModoOscuro.collectAsState(initial = false)
            val isLoggedIn by sessionPreferences.isUserLoggedIn.collectAsState(initial = false) // Estado de la sesión


            val eventViewModel: EventViewModel = viewModel(
                factory = EventViewModelFactory(repository)
            )


            val loginViewModel: LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return LoginViewModel(sessionPreferences) as T
                    }
                }
            )


            val windowSize = calculateWindowSizeClass(this)
            val esTablet = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded

            TechEventTheme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {

                    MainNavigationContainer(
                        eventViewModel = eventViewModel,
                        loginViewModel = loginViewModel,
                        esTablet = esTablet,
                        isDarkMode = isDarkMode,
                        isLoggedIn = isLoggedIn,
                        onToggleTheme = { nuevoValor ->
                            scope.launch { themePreferences.guardarModoOscuro(nuevoValor) }
                        }
                    )
                }
            }
        }
    }
}