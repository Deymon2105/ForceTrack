package com.example.forcetrack

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

// DataStore delegate
private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_CURRENT_USER_ID = intPreferencesKey("current_user_id")
    }

    val currentUserIdFlow: Flow<Int?> = context.dataStore.data
        .map { prefs -> prefs[KEY_CURRENT_USER_ID] }

    suspend fun saveCurrentUserId(userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_USER_ID] = userId
        }
    }

    suspend fun clearCurrentUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CURRENT_USER_ID)
        }
    }

    // Lectura síncrona (una sola vez) del id actual
    suspend fun getCurrentUserId(): Int? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_CURRENT_USER_ID]
    }
}
