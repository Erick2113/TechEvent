package com.example.techevent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techevent.data.SessionPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class LoginUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = ""
)

class LoginViewModel(private val sessionPreferences: SessionPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    //  Función para iniciar sesión
    fun login(user: String, pass: String) {
        viewModelScope.launch {
            // Estado: Loading
            _uiState.update { it.copy(isLoading = true, isError = false) }

            delay(1500)

            // Validación quemada exigida por la rúbrica
            if (user == "admin" && pass == "1234") {
                sessionPreferences.guardarSesion(true) // Guardamos en DataStore
                // Estado: Success
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                // Estado: Error
                _uiState.update {
                    it.copy(isLoading = false, isError = true, errorMessage = "Credenciales incorrectas")
                }
            }
        }
    }

    //  Función para cerrar sesión desde Ajustes
    fun logout() {
        viewModelScope.launch {
            sessionPreferences.guardarSesion(false) // Borramos la sesión del DataStore
            _uiState.value = LoginUiState()         // Reseteamos la pantalla
        }
    }


    fun resetState() {
        _uiState.value = LoginUiState()
    }
}