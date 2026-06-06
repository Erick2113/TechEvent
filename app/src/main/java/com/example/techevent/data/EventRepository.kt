package com.example.techevent.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EventRepository(
    private val dao: EventoDao,
    private val api: TechEventApiService
) {
    val favoritos: Flow<List<EventoFavoritoEntity>> = dao.obtenerTodosLosFavoritos()

    suspend fun obtenerCatalogoEventos(): List<EventoNetwork> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerEventos()
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            } else {
                throw Exception("Error del servidor")
            }
        } catch (e: Exception) {
            throw Exception("OFFLINE_MODE")
        }
    }

    suspend fun guardarFavorito(evento: EventoNetwork) {
        dao.insertarFavorito(EventoFavoritoEntity(evento.id, evento.titulo, evento.fecha, evento.bannerUrl))
    }

    suspend fun eliminarFavorito(id: String) {
        dao.eliminarFavorito(EventoFavoritoEntity(id, "", "", ""))
    }
}