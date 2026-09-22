package com.example.gamezone.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.data.model.Game
import com.example.gamezone.data.repository.MockDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ExploreViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val categories = listOf("Todos", "Action RPG", "Action Adventure", "RPG", "Action", "Adventure", "Open World", "Metroidvania", "Platformer", "Indie")

    val filteredGames: StateFlow<List<Game>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        MockDataProvider.games.filter { game ->
            val matchesQuery = game.title.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Todos") true else game.genre.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MockDataProvider.games
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "Todos"
    }
}
