package com.example.gamezone.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.gamezone.data.model.Game
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PremiumGold

@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Se muestra cuando el juego es exclusivo Pro y quien lo esta viendo no
    // tiene acceso activo a Premium (invitado, cuenta Free, o Pro sin sesion).
    // Por defecto false para no afectar a las pantallas que no lo necesitan.
    isLocked: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Card(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Eliminamos el ripple para un efecto más limpio
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
            ) {
                SubcomposeAsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    onSuccess = {
                        Log.d("GameCard", "SUCCESS: ${game.title}")
                    },
                    onError = { state ->
                        Log.e("GameCard", "FAILED: ${game.title} | Error: ${state.result.throwable.message}")
                    },
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryNeon)
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )
                
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = PremiumGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (game.rating > 0) game.rating.toString() else "TBA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                if (isLocked) {
                    PremiumBadge(modifier = Modifier.padding(8.dp).align(Alignment.TopStart))
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = game.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FeaturedGameCard(
    game: Game,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    Card(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDetailsClick
            ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = game.imageUrl,
                contentDescription = game.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                onSuccess = {
                    Log.d("FeaturedGameCard", "SUCCESS: ${game.title}")
                },
                onError = { state ->
                    Log.e("FeaturedGameCard", "FAILED: ${game.title} | Error: ${state.result.throwable.message}")
                },
                loading = {
                    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.95f)
                            ),
                            startY = 100f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = PrimaryNeon,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "DESTACADO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.genre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = game.rating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
            
            Button(
                onClick = onDetailsClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Detalles", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
