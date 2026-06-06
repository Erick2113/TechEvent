package com.example.techevent.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

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

@Database(entities = [EventoFavoritoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventoDao(): EventoDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "techevent_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}




interface TechEventApiService {

    @GET("344b23638686f3d6058f")
    suspend fun obtenerEventos(): Response<List<EventoNetwork>>
}

object RetrofitClient {
    // 👇 URL Base del servidor
    private const val BASE_URL = "https://api.npoint.io/"

    val apiService: TechEventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TechEventApiService::class.java)
    }
}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_pref")

class ThemePreferences(private val context: Context) {
    companion object { val MODO_OSCURO_KEY = booleanPreferencesKey("modo_oscuro_habilitado") }
    val esModoOscuro: Flow<Boolean> = context.dataStore.data.map { it[MODO_OSCURO_KEY] ?: false }
    suspend fun guardarModoOscuro(habilitado: Boolean) {
        context.dataStore.edit { it[MODO_OSCURO_KEY] = habilitado }
    }
}