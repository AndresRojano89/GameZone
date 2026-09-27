package com.example.gamezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gamezone.data.model.GameCategory
import com.example.gamezone.data.repository.MockDataProvider
import com.example.gamezone.ui.components.*
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    onGameClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onGoToPremiumClick: () -> Unit,
    onViewAllClick: () -> Unit,
    viewModel: AppViewModel
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val username by viewModel.username.collectAsState()
    val games = MockDataProvider.games
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val featuredGame = games.firstOrNull { it.category == GameCategory.FEATURED }
    val trendingGames = games.filter { it.category == GameCategory.TRENDING }
    val topRatedGames = games.filter { it.category == GameCategory.TOP_RATED }
    val comingSoonGames = games.filter { it.category == GameCategory.COMING_SOON }
    val retroGames = games.filter { it.category == GameCategory.RETRO }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { 40 },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Header
                item {
                    HomeHeader(
                        isPremium = isPremium,
                        greetingName = if (isLoggedIn) username else "Jugador",
                        onProfileClick = onProfileClick
                    )
                }

                // 2. Featured Game
                featuredGame?.let {
                    item {
                        FeaturedGameCard(
                            game = it,
                            onDetailsClick = { onGameClick(it.id) },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // 3. Trending
                item { SectionHeader(title = "Tendencias", onViewAllClick = onViewAllClick) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(trendingGames) { game ->
                            GameCard(
                                game = game, 
                                onClick = { onGameClick(game.id) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }

                // 4. Secciones de contenido (Espaciado sutil)
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 5. Top Rated
                item { SectionHeader(title = "Mejor valorados", onViewAllClick = onViewAllClick) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(topRatedGames) { game ->
                            GameCard(
                                game = game, 
                                onClick = { onGameClick(game.id) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }

                // 6. Premium Content: Retro Games
                item {
                    Crossfade(targetState = isPremium, label = "premium_content") { premium ->
                        if (premium) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                SectionHeader(title = "GameZone Retro 🕹️", onViewAllClick = null)
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(retroGames) { game ->
                                        GameCard(
                                            game = game, 
                                            onClick = { onGameClick(game.id) },
                                            modifier = Modifier.width(160.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            LockedRetroSection(onGoToPremiumClick)
                        }
                    }
                }

                // 7. Coming Soon
                item { SectionHeader(title = "Próximamente", onViewAllClick = onViewAllClick) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(comingSoonGames) { game ->
                            GameCard(
                                game = game, 
                                onClick = { onGameClick(game.id) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LockedRetroSection(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SectionHeader(title = "GameZone Retro", onViewAllClick = null)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Contenido Exclusivo Pro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Desbloquea la colección Retro con GameZone PRO",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onClick) {
                    Text("Ver GameZone Pro", color = PrimaryNeon, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    isPremium: Boolean,
    greetingName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, $greetingName",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                GameZoneLogo(isCompact = true)
                if (isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    PremiumBadge()
                }
            }
        }
        
        IconButton(
            onClick = onProfileClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                tint = PrimaryNeon
            )
        }
    }
}
