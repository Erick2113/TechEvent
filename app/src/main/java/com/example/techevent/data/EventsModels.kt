package com.example.techevent.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class EventoNetwork(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val estado: String,
    val bannerUrl: String,
    val ponentes: String,
    val agenda: String,
    val lugar: String
)

@Entity(tableName = "favoritos_table")
data class EventoFavoritoEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val fecha: String,
    val bannerUrl: String
)