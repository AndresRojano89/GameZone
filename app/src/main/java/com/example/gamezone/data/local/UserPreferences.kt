package com.example.gamezone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val LIBRARY_IDS = stringSetPreferencesKey("library_ids")
        private val USERNAME = stringPreferencesKey("username")
        private val HAS_PROFILE = booleanPreferencesKey("has_profile")
        private val AVATAR_INDEX = intPreferencesKey("avatar_index")
        private val USER_RATINGS = stringPreferencesKey("user_ratings")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PREMIUM] ?: false
    }

    val libraryIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[LIBRARY_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    val username: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USERNAME] ?: "Invitado"
    }

    val hasProfile: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_PROFILE] ?: false
    }

    val avatarIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AVATAR_INDEX] ?: 0
    }

    // Formato: "id:rating,id:rating"
    val userRatings: Flow<Map<Int, Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[USER_RATINGS] ?: ""
        if (raw.isEmpty()) return@map emptyMap()
        raw.split(",").associate {
            val parts = it.split(":")
            parts[0].toInt() to parts[1].toInt()
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setPremium(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_PREMIUM] = active
        }
    }

    suspend fun saveLibraryIds(ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[LIBRARY_IDS] = ids.map { it.toString() }.toSet()
        }
    }

    suspend fun setProfileInfo(name: String, avatar: Int) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME] = name
            prefs[AVATAR_INDEX] = avatar
            prefs[HAS_PROFILE] = true
        }
    }

    suspend fun setHasProfile(hasProfile: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_PROFILE] = hasProfile
        }
    }

    suspend fun saveRating(gameId: Int, rating: Int) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[USER_RATINGS] ?: ""
            val currentMap = if (currentRaw.isEmpty()) mutableMapOf() 
                            else currentRaw.split(",").associate {
                                val parts = it.split(":")
                                parts[0].toInt() to parts[1].toInt()
                            }.toMutableMap()
            
            currentMap[gameId] = rating
            prefs[USER_RATINGS] = currentMap.map { "${it.key}:${it.value}" }.joinToString(",")
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
