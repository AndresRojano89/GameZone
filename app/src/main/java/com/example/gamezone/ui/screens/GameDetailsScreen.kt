package com.example.gamezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.gamezone.data.repository.MockDataProvider
import com.example.gamezone.ui.theme.PremiumGold
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.viewmodel.AppViewModel

@Composable
fun GameDetailsScreen(
    gameId: Int,
    onBackClick: () -> Unit,
    onCreateProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AppViewModel
) {
    val game = MockDataProvider.games.find { it.id == gameId }
    val libraryIds by viewModel.libraryGameIds.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()
    val userRatings by viewModel.userRatings.collectAsState()
    
    // Sin sesión activa no se muestra actividad personal (biblioteca/valoración)
    // de una cuenta anterior en este mismo dispositivo.
    val isInLibrary = isLoggedIn && libraryIds.contains(gameId)
    val userRating = if (isLoggedIn) userRatings[gameId] ?: 0 else 0
    
    var isVisible by remember { mutableStateOf(false) }
    var showGuestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    if (game == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Juego no encontrado", color = Color.White)
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = game.imageUrl,
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryNeon)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    )

                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, MaterialTheme.colorScheme.background),
                                startY = 0f
                            )
                        )
                    )

                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                }

                // Details
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-30).dp)
                ) {
                    Surface(color = PrimaryNeon.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PrimaryNeon.copy(alpha = 0.5f))) {
                        Text(text = game.genre.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = PrimaryNeon, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = game.title, style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.ExtraBold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = game.rating.toString(), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(text = if (game.isPremium) "Exclusivo Pro" else "Gratuito", style = MaterialTheme.typography.bodyMedium, color = if (game.isPremium) PrimaryNeon else Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Library Button
                    val containerColor by animateColorAsState(if (isInLibrary) MaterialTheme.colorScheme.surfaceVariant else PrimaryNeon, label = "")
                    val contentColor by animateColorAsState(if (isInLibrary) Color.White else Color.Black, label = "")

                    Button(
                        onClick = { if (isLoggedIn) viewModel.toggleLibraryGame(gameId) else showGuestDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AnimatedContent(targetState = isInLibrary, label = "") { saved ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = if (saved) "En tu Biblioteca" else "Agregar a Biblioteca", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Rating
                    Text("Tu valoración", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (userRating >= star) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (userRating >= star) PremiumGold else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp).clickable {
                                    if (isLoggedIn) viewModel.setRating(gameId, star) else showGuestDialog = true
                                }.padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Sobre el juego", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = game.description, style = MaterialTheme.typography.bodyLarge, color = Color.LightGray, lineHeight = 24.sp)

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Plataformas", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = game.platforms.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text(if (hasProfile) "Inicia sesión para continuar" else "Personaliza tu GameZone") },
            text = {
                Text(
                    if (hasProfile) "Ya tienes una cuenta local en este dispositivo. Inicia sesión para guardar juegos en tu biblioteca y valorarlos."
                    else "Crea un perfil para guardar juegos en tu biblioteca, valorarlos y conservar tus preferencias."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGuestDialog = false
                        if (hasProfile) onLoginClick() else onCreateProfileClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black)
                ) {
                    Text(if (hasProfile) "Iniciar sesión" else "Crear perfil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text("Ahora no", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}
