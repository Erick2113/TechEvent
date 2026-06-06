package com.example.techevent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.techevent.data.EventoFavoritoEntity
import com.example.techevent.data.EventoNetwork
import com.example.techevent.data.EventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class EventoUiState {
    object Loading : EventoUiState()
    data class Success(val lista: List<EventoNetwork>, val esOffline: Boolean = false) : EventoUiState()
    data class Error(val mensaje: String) : EventoUiState()
}

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<EventoUiState>(EventoUiState.Loading)
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    private val _idFavoritos = MutableStateFlow<Set<String>>(emptySet())
    val idFavoritos: StateFlow<Set<String>> = _idFavoritos.asStateFlow()

    private var listaLocalFavoritos = emptyList<EventoFavoritoEntity>()

    init {
        viewModelScope.launch {
            repository.favoritos.collect { listaFavs ->
                listaLocalFavoritos = listaFavs
                _idFavoritos.value = listaFavs.map { it.id }.toSet()
            }
        }
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _uiState.value = EventoUiState.Loading
            try {
                val datos = repository.obtenerCatalogoEventos()
                _uiState.value = EventoUiState.Success(lista = datos, esOffline = false)
            } catch (e: Exception) {
                if (e.message == "OFFLINE_MODE") {
                    val datosOffline = listaLocalFavoritos.map {
                        EventoNetwork(it.id, it.titulo, "Modo offline", it.fecha, "Guardado Local", it.bannerUrl, "", "", "")
                    }
                    _uiState.value = EventoUiState.Success(lista = datosOffline, esOffline = true)
                } else {
                    _uiState.value = EventoUiState.Error(e.localizedMessage ?: "Error Desconocido")
                }
            }
        }
    }

    fun toggleFavorito(evento: EventoNetwork) {
        viewModelScope.launch {
            if (_idFavoritos.value.contains(evento.id)) {
                repository.eliminarFavorito(evento.id)
            } else {
                repository.guardarFavorito(evento)
            }
        }
    }
}


class EventViewModelFactory(private val repository: EventRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}