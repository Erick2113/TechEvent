package com.example.techevent.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFavorito(evento: EventoFavoritoEntity)

    @Delete
    suspend fun eliminarFavorito(evento: EventoFavoritoEntity)

    @Query("SELECT * FROM favoritos_table")
    fun obtenerTodosLosFavoritos(): Flow<List<EventoFavoritoEntity>>

    @Query("SELECT EXISTS(SELECT * FROM favoritos_table WHERE id = :id)")
    suspend fun esFavorito(id: String): Boolean
}