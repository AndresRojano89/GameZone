package com.example.gamezone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PrimaryVariant
import com.example.gamezone.ui.viewmodel.AppViewModel

@Composable
fun CreateProfileScreen(
    onBackClick: () -> Unit,
    viewModel: AppViewModel
) {
    val currentHasProfile by viewModel.hasProfile.collectAsState()
    val currentUsername by viewModel.username.collectAsState()
    val currentAvatarIndex by viewModel.avatarIndex.collectAsState()

    var name by remember { mutableStateOf(if (currentHasProfile) currentUsername else "") }
    var selectedAvatar by remember { mutableIntStateOf(if (currentHasProfile) currentAvatarIndex else 0) }

    val minNameLength = 3
    val maxNameLength = 15
    val trimmedName = name.trim()
    val nameError: String? = when {
        name.isEmpty() -> null
        trimmedName.isEmpty() -> "El nombre no puede contener solo espacios"
        trimmedName.length < minNameLength -> "El nombre debe tener al menos $minNameLength caracteres"
        else -> null
    }
    val isNameValid = trimmedName.length in minNameLength..maxNameLength

    val avatarColors = listOf(
        listOf(PrimaryNeon, PrimaryVariant),
        listOf(Color(0xFF03DAC6), Color(0xFF018786)),
        listOf(Color(0xFFF44336), Color(0xFFB71C1C)),
        listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D)),
        listOf(Color(0xFF2196F3), Color(0xFF0D47A1))
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = if (currentHasProfile) "Editar Perfil" else "Crear Perfil Local",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Avatar Preview
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(avatarColors[selectedAvatar]),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Personaliza tu identidad visual",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Text(
                text = "Elige un tema de color para tu avatar.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(avatarColors) { index, colors ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(brush = Brush.linearGradient(colors))
                            .border(
                                width = if (selectedAvatar == index) 3.dp else 0.dp,
                                color = if (selectedAvatar == index) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedAvatar = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatar == index) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= maxNameLength) name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de usuario") },
                placeholder = { Text("Escribe tu nombre...") },
                singleLine = true,
                isError = nameError != null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = PrimaryNeon,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (nameError != null) {
                    Text(nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(
                    text = "${name.length}/$maxNameLength",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isNameValid) {
                        viewModel.createProfile(trimmedName, selectedAvatar)
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isNameValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentHasProfile) "Guardar Cambios" else "Comenzar mi Aventura",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
