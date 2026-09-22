package com.example.gamezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gamezone.data.model.Game
import com.example.gamezone.data.repository.MockDataProvider
import com.example.gamezone.ui.components.PremiumBadge
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PrimaryVariant
import com.example.gamezone.ui.viewmodel.AppViewModel

@Composable
fun ProfileScreen(
    onGoToPremiumClick: () -> Unit,
    onCreateProfileClick: () -> Unit,
    onGameClick: (Int) -> Unit,
    viewModel: AppViewModel
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val libraryIds by viewModel.libraryGameIds.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()
    val username by viewModel.username.collectAsState()
    val avatarIndex by viewModel.avatarIndex.collectAsState()
    val userRatings by viewModel.userRatings.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    
    val totalGames = MockDataProvider.games.size
    var isVisible by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    val avatarColors = listOf(
        listOf(PrimaryNeon, PrimaryVariant),
        listOf(Color(0xFF03DAC6), Color(0xFF018786)),
        listOf(Color(0xFFF44336), Color(0xFFB71C1C)),
        listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D)),
        listOf(Color(0xFF2196F3), Color(0xFF0D47A1))
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400)),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Header
                item { 
                    ProfileHeader(
                        hasProfile = hasProfile,
                        username = username,
                        isPremium = isPremium,
                        avatarColors = avatarColors[avatarIndex.coerceIn(0, 4)],
                        onActionClick = onCreateProfileClick
                    ) 
                }
                
                // 2. Stats
                item {
                    ProfileStatsSection(
                        libraryCount = libraryIds.size, 
                        ratingCount = userRatings.size,
                        catalogCount = totalGames 
                    )
                }

                // 3. Activity (Recent Library)
                if (hasProfile && libraryIds.isNotEmpty()) {
                    item {
                        ActivitySection(
                            title = "Mi Biblioteca Reciente",
                            gameIds = libraryIds.take(5).toList(),
                            onGameClick = onGameClick
                        )
                    }
                }
                
                // 4. Premium CTA
                item { 
                    AnimatedContent(targetState = isPremium, label = "premium_card") { premium ->
                        PremiumCallToAction(isPremium = premium, onClick = onGoToPremiumClick)
                    }
                }
                
                // 5. Settings
                item {
                    ProfileSettingsSection(
                        hasProfile = hasProfile,
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = { viewModel.setNotificationsEnabled(it) },
                        onAccountClick = { showAccountDialog = true },
                        onAboutClick = { showAboutDialog = true }
                    ) 
                }
                
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text(text = "GameZone v1.0.0", style = MaterialTheme.typography.bodySmall, color = Color.Gray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showAccountDialog) {
        AccountDialog(
            username = username,
            hasProfile = hasProfile,
            isPremium = isPremium,
            onLogout = { 
                viewModel.logout()
                showAccountDialog = false 
            },
            onEdit = {
                showAccountDialog = false
                onCreateProfileClick()
            },
            onDismiss = { showAccountDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(
    hasProfile: Boolean,
    username: String,
    isPremium: Boolean,
    avatarColors: List<Color>,
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = if (hasProfile) Brush.linearGradient(avatarColors) 
                            else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray)), 
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasProfile) Icons.Default.Person else Icons.Default.AccountCircle, 
                contentDescription = null, 
                modifier = Modifier.size(50.dp), 
                tint = if (hasProfile) Color.Black else Color.Gray
            )
            
            if (hasProfile) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PrimaryNeon, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = username, 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Bold, 
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!hasProfile) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    onClick = onActionClick
                ) {
                    Text(
                        text = "Invitado • Crear Perfil", 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = PrimaryNeon
                    )
                }
            } else if (isPremium) {
                PremiumBadge()
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(16.dp))
            } else {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                    Text(text = "Cuenta Gratuita", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ProfileStatsSection(libraryCount: Int, ratingCount: Int, catalogCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(label = "Biblioteca", value = libraryCount.toString(), icon = Icons.Default.Favorite, modifier = Modifier.weight(1f))
        StatCard(label = "Valorados", value = ratingCount.toString(), icon = Icons.Default.Star, modifier = Modifier.weight(1f))
        StatCard(label = "Catálogo", value = catalogCount.toString(), icon = Icons.Default.List, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, contentDescription = null, tint = PrimaryNeon.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ActivitySection(title: String, gameIds: List<Int>, onGameClick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(gameIds) { id ->
                val game = MockDataProvider.games.find { it.id == id }
                game?.let {
                    Card(
                        modifier = Modifier
                            .size(width = 80.dp, height = 110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onGameClick(it.id) }
                    ) {
                        AsyncImage(
                            model = it.imageUrl,
                            contentDescription = it.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumCallToAction(isPremium: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) MaterialTheme.colorScheme.surfaceVariant else PrimaryVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = if (isPremium) "Suscripción GameZone PRO" else "Consigue GameZone PRO", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPremium) "Disfrutas de todas las ventajas exclusivas: Sin anuncios y Colección Retro." 
                       else "Elimina la publicidad y accede a contenido retro exclusivo por un precio mensual.", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPremium) MaterialTheme.colorScheme.surface else PrimaryNeon, 
                    contentColor = if (isPremium) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (isPremium) "Gestionar Suscripción" else "Ver Planes Pro", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileSettingsSection(
    hasProfile: Boolean,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onAccountClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        
        SettingsItem(
            icon = Icons.Default.AccountCircle, 
            title = "Cuenta", 
            value = if (hasProfile) "Gestionar" else "Crear perfil", 
            onClick = onAccountClick
        )
        
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
            Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Notificaciones", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    Text(text = if (notificationsEnabled) "Activadas" else "Desactivadas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryNeon,
                        checkedTrackColor = PrimaryNeon.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        SettingsItem(icon = Icons.Default.Info, title = "Sobre GameZone", value = "", onClick = onAboutClick)
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, onClick = onClick) {
        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            if (value.isNotEmpty()) {
                Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sobre GameZone", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("GameZone es una plataforma de descubrimiento y seguimiento de videojuegos diseñada para gamers apasionados.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tecnologías Principales:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text("• Kotlin & Jetpack Compose\n• Material 3 Design\n• DataStore Persistence\n• Navigation Compose\n• MVVM Architecture", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Versión: 1.0.0 (Academic Project)", style = MaterialTheme.typography.labelSmall, color = PrimaryNeon)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = PrimaryNeon)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun AccountDialog(
    username: String,
    hasProfile: Boolean,
    isPremium: Boolean,
    onLogout: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestión de Cuenta", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Usuario actual: $username", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text(
                    text = if (isPremium) "Suscripción: GameZone PRO" else "Suscripción: Gratuita",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPremium) PrimaryNeon else Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (hasProfile) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Perfil")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679), contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                } else {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black)
                    ) {
                        Text("Crear Perfil Ahora")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: El cierre de sesión te mantendrá como visitante pero conservará tus datos locales.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PrimaryNeon)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
