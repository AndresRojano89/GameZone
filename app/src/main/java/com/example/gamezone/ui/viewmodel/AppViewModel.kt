package com.example.gamezone.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.data.local.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    val isPremium: StateFlow<Boolean> = userPreferences.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val libraryGameIds: StateFlow<Set<Int>> = userPreferences.libraryIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val username: StateFlow<String> = userPreferences.username
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Invitado")

    val hasProfile: StateFlow<Boolean> = userPreferences.hasProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val avatarIndex: StateFlow<Int> = userPreferences.avatarIndex
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val userRatings: StateFlow<Map<Int, Int>> = userPreferences.userRatings
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val notificationsEnabled: StateFlow<Boolean> = userPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setPremium(active: Boolean) {
        viewModelScope.launch {
            userPreferences.setPremium(active)
        }
    }

    fun togglePremium() {
        setPremium(!isPremium.value)
    }

    fun toggleLibraryGame(gameId: Int) {
        viewModelScope.launch {
            val currentIds = libraryGameIds.value
            val newIds = if (currentIds.contains(gameId)) {
                currentIds - gameId
            } else {
                currentIds + gameId
            }
            userPreferences.saveLibraryIds(newIds)
        }
    }

    fun createProfile(name: String, avatar: Int) {
        viewModelScope.launch {
            userPreferences.setProfileInfo(name, avatar)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.setHasProfile(false)
        }
    }

    fun setRating(gameId: Int, rating: Int) {
        viewModelScope.launch {
            userPreferences.saveRating(gameId, rating)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun isInLibrary(gameId: Int): Boolean {
        return libraryGameIds.value.contains(gameId)
    }
}
