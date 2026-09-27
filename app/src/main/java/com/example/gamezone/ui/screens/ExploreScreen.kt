package com.example.gamezone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamezone.ui.components.GameCard
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.viewmodel.AppViewModel
import com.example.gamezone.ui.viewmodel.ExploreViewModel

@Composable
fun ExploreScreen(
    onGameClick: (Int) -> Unit,
    appViewModel: AppViewModel,
    viewModel: ExploreViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val games by viewModel.filteredGames.collectAsState()
    // Explorar filtra por genero, no por categoria, asi que sin esta bandera
    // los juegos exclusivos de la Coleccion Retro eran completamente visibles
    // y accesibles para invitados/cuentas Free, sin ninguna senal de que
    // requieren GameZone Pro (inconsistente con el bloqueo que ya existe en Home).
    val hasActivePremiumAccess by appViewModel.hasActivePremiumAccess.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Header & Search
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Explorar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Encuentra tu próximo juego favorito",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar juegos...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryNeon) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    singleLine = true
                )
            }

            // 2. Categories Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(viewModel.categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNeon,
                            selectedLabelColor = Color.Black,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            disabledSelectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // 3. Results Grid
            if (games.isEmpty()) {
                EmptyResults(onClear = { viewModel.clearFilters() })
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(games) { game ->
                        GameCard(
                            game = game,
                            onClick = { onGameClick(game.id) },
                            modifier = Modifier.fillMaxWidth(),
                            isLocked = game.isPremium && !hasActivePremiumAccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyResults(onClear: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sin resultados 🔍",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No encontramos juegos que coincidan con tu búsqueda.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Limpiar filtros", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
