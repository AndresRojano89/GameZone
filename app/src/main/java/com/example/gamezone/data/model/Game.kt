package com.example.gamezone.data.model

data class Game(
    val id: Int,
    val title: String,
    val description: String,
    val rating: Double,
    val genre: String,
    val imageUrl: String,
    val platforms: List<String> = listOf("PS5", "Xbox", "PC"),
    val isPremium: Boolean = false,
    val category: GameCategory = GameCategory.TRENDING
)

enum class GameCategory {
    FEATURED, TRENDING, TOP_RATED, COMING_SOON, RETRO
}
