package com.example.gamezone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.viewmodel.AppViewModel

private val minPasswordLength = 4

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryNeon,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = PrimaryNeon,
    unfocusedLabelColor = Color.Gray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorBorderColor = Color(0xFFCF6679),
    errorLabelColor = Color(0xFFCF6679)
)

/**
 * Pantalla de inicio de sesion local.
 *
 * Si la cuenta guardada en el dispositivo aun no tiene contrasena configurada
 * (cuentas creadas antes de este sistema de credenciales), se muestra primero
 * un paso de migracion para establecerla, sin perder biblioteca, valoraciones
 * ni el estado de GameZone Pro. Una vez que hay credenciales, siempre se exige
 * usuario + contrasena: nunca se recupera la sesion solo porque exista un perfil.
 */
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AppViewModel
) {
    val hasCredentials by viewModel.hasCredentials.collectAsState()
    val storedUsername by viewModel.username.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = if (hasCredentials) "Iniciar Sesión" else "Configura tu contraseña",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (hasCredentials) {
                LoginForm(
                    onSubmit = { username, password, onError ->
                        viewModel.login(username, password) { success ->
                            if (success) onLoginSuccess() else onError()
                        }
                    }
                )
            } else {
                SetCredentialsForm(
                    username = storedUsername,
                    onSubmit = { password ->
                        viewModel.setCredentials(password)
                        onLoginSuccess()
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    onSubmit: (username: String, password: String, onError: () -> Unit) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val isValid = username.isNotBlank() && password.isNotEmpty()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = PrimaryNeon
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ingresa tus credenciales de tu cuenta local para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = username,
        onValueChange = { username = it; showError = false },
        label = { Text("Nombre de usuario") },
        singleLine = true,
        isError = attemptedSubmit && username.isBlank(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = loginFieldColors()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it; showError = false },
        label = { Text("Contraseña") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        isError = attemptedSubmit && password.isEmpty(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = loginFieldColors()
    )

    if (showError) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Usuario o contraseña incorrectos.",
            color = Color(0xFFCF6679),
            style = MaterialTheme.typography.labelMedium
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            attemptedSubmit = true
            if (isValid) {
                onSubmit(username.trim(), password) { showError = true }
            }
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = isValid,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SetCredentialsForm(
    username: String,
    onSubmit: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }

    val passwordValid = password.length >= minPasswordLength
    val passwordsMatch = password == confirmPassword
    val isValid = passwordValid && passwordsMatch

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = PrimaryNeon
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Encontramos tu cuenta local \"$username\". Antes de continuar, crea una contraseña para poder iniciar sesión de nuevo en el futuro. Tu biblioteca, valoraciones y GameZone Pro se conservan.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Nueva contraseña") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        isError = attemptedSubmit && !passwordValid,
        supportingText = {
            if (attemptedSubmit && !passwordValid) {
                Text("Mínimo $minPasswordLength caracteres")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = loginFieldColors()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = { Text("Confirmar contraseña") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        isError = attemptedSubmit && !passwordsMatch,
        supportingText = {
            if (attemptedSubmit && !passwordsMatch) {
                Text("Las contraseñas no coinciden")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = loginFieldColors()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            attemptedSubmit = true
            if (isValid) onSubmit(password)
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Guardar y continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
