package com.example.gamezone.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Explore : Screen("explore", "Explorar", Icons.Default.Search)
    object Library : Screen("library", "Biblioteca", Icons.Default.Favorite)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object CreateProfile : Screen("create_profile", "Crear Perfil", null)
    object Premium : Screen("premium", "Premium", null)
    object Details : Screen("details/{gameId}", "Detalles", null) {
        fun createRoute(gameId: Int) = "details/$gameId"
    }
}