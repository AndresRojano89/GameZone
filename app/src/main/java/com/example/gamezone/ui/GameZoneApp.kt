package com.example.gamezone.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gamezone.navigation.Screen
import com.example.gamezone.ui.screens.*
import com.example.gamezone.ui.viewmodel.AppViewModel

@Composable
fun GameZoneApp() {
    val navController = rememberNavController()
    val appViewModel: AppViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Home,
        Screen.Explore,
        Screen.Library,
        Screen.Profile
    )

    // Función centralizada para navegar a destinos principales
    val navigateToTopLevel = { route: String ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == Screen.Home.route || currentRoute == Screen.Explore.route || 
                currentRoute == Screen.Library.route || currentRoute == Screen.Profile.route) {
                
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navigateToTopLevel(screen.route)
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Details.createRoute(gameId))
                    },
                    onProfileClick = {
                        navigateToTopLevel(Screen.Profile.route)
                    },
                    onGoToPremiumClick = {
                        navController.navigate(Screen.Premium.route)
                    },
                    onViewAllClick = {
                        navigateToTopLevel(Screen.Explore.route)
                    },
                    viewModel = appViewModel
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Details.createRoute(gameId))
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Details.createRoute(gameId))
                    },
                    onExploreClick = {
                        navigateToTopLevel(Screen.Explore.route)
                    },
                    onCreateProfileClick = {
                        navController.navigate(Screen.CreateProfile.route)
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    viewModel = appViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onGoToPremiumClick = {
                        navController.navigate(Screen.Premium.route)
                    },
                    onCreateProfileClick = {
                        navController.navigate(Screen.CreateProfile.route)
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Details.createRoute(gameId))
                    },
                    viewModel = appViewModel
                )
            }
            composable(
                route = Screen.Details.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: 0
                GameDetailsScreen(
                    gameId = gameId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCreateProfileClick = {
                        navController.navigate(Screen.CreateProfile.route)
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    viewModel = appViewModel
                )
            }
            composable(
                route = Screen.Premium.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }) }
            ) {
                PremiumScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCreateProfileClick = {
                        navController.navigate(Screen.CreateProfile.route)
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    viewModel = appViewModel
                )
            }
            composable(Screen.CreateProfile.route) {
                CreateProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    viewModel = appViewModel
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        navController.popBackStack()
                    },
                    viewModel = appViewModel
                )
            }
        }
    }
}
