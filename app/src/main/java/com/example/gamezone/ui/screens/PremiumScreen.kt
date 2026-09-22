package com.example.gamezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamezone.ui.components.GameZoneLogo
import com.example.gamezone.ui.components.PremiumBadge
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PrimaryVariant
import com.example.gamezone.ui.viewmodel.AppViewModel

@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    viewModel: AppViewModel
) {
    val isPremium by viewModel.isPremium.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 20 },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    GameZoneLogo(isCompact = true)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = PrimaryNeon.copy(alpha = 0.1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = PrimaryNeon
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isPremium) "Miembro GameZone PRO" else "Únete a GameZone PRO",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    if (isPremium) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PremiumBadge()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPremium) 
                            "Disfrutas de una experiencia premium completa y sin interrupciones." 
                            else "Eleva tu experiencia gamer al siguiente nivel con funciones exclusivas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Benefits List
                    BenefitCard {
                        BenefitItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Sin Publicidad",
                            description = "Navega por todo el catálogo sin banners de anuncios."
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                        BenefitItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Colección Retro",
                            description = "Acceso desbloqueado a los clásicos más legendarios."
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                        BenefitItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Insignia Exclusiva",
                            description = "Destaca tu perfil con el sello PRO oficial."
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    if (!isPremium) {
                        // Pricing & Plans
                        PlanCard(
                            title = "PLAN ANUAL",
                            price = "$39.99",
                            period = "/año",
                            savings = "Ahorra 33%",
                            onClick = { showConfirmDialog = true }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PlanCard(
                            title = "PLAN MENSUAL",
                            price = "$4.99",
                            period = "/mes",
                            isRecommended = true,
                            onClick = { showConfirmDialog = true }
                        )
                    } else {
                        // Management for Pro Users
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryNeon.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp),
                            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PrimaryNeon.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Estado: Suscripción Activa",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrimaryNeon,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tu plan se renueva automáticamente cada mes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedButton(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Gestionar / Cancelar Suscripción")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Text(
                        text = "Información Legal: Esta es una demostración académica. No se procesarán pagos reales.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Dialogs
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Suscripción", fontWeight = FontWeight.Bold) },
            text = { 
                Text("¿Deseas activar GameZone Pro?\n\nAccederás instantáneamente a todos los beneficios. Esta acción es una simulación local.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPremium(true)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black)
                ) {
                    Text("Activar Ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("¿Cancelar Suscripción?", fontWeight = FontWeight.Bold) },
            text = { 
                Text("Perderás el acceso a la colección Retro y volverás a ver anuncios. Puedes volver a unirte cuando quieras.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPremium(false)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679), contentColor = Color.White)
                ) {
                    Text("Confirmar Cancelación")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Mantener Pro", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun BenefitCard(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun BenefitItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryNeon,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    savings: String? = null,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) PrimaryVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isRecommended) AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PrimaryNeon) else null
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (isRecommended) {
                    Text("RECOMENDADO", style = MaterialTheme.typography.labelSmall, color = PrimaryNeon, fontWeight = FontWeight.Bold)
                }
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                if (savings != null) {
                    Text(text = savings, style = MaterialTheme.typography.labelSmall, color = Color(0xFF03DAC6))
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = price, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = period, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}
