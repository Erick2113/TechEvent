package com.example.techevent.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_pref")

class SessionPreferences(private val context: Context) {
    companion object {
        val LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }


    val isUserLoggedIn: Flow<Boolean> = context.sessionDataStore.data.map { preferences ->
        preferences[LOGGED_IN_KEY] ?: false
    }


    suspend fun guardarSesion(isLoggedIn: Boolean) {
        context.sessionDataStore.edit { preferences ->
            preferences[LOGGED_IN_KEY] = isLoggedIn
        }
    }
}