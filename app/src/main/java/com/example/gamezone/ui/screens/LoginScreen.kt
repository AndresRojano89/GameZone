package com.example.gamezone.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gamezone.data.local.LoginResult
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.viewmodel.AppViewModel

private val ErrorColor = Color(0xFFCF6679)

/**
 * Pantalla de inicio de sesion local.
 *
 * Siempre muestra el formulario usuario + contrasena: nunca se recupera ni se
 * sugiere ninguna cuenta por el simple hecho de que exista en el dispositivo.
 * La cuenta se busca a partir de lo que el usuario escribe y, si las
 * credenciales coinciden, se inicia sesion exactamente en esa cuenta.
 *
 * Unico caso especial: si el usuario escribe el nombre de una cuenta creada
 * antes del sistema de credenciales (sin contrasena), se le pide crear su
 * contrasena para esa cuenta, conservando biblioteca, valoraciones y Premium.
 */
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit,
    viewModel: AppViewModel
) {
    // Nombre de la cuenta antigua (escrito por el usuario) que necesita
    // configurar su contrasena; null mientras se muestra el login normal.
    var accountNeedingPassword by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = accountNeedingPassword != null) { accountNeedingPassword = null }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (accountNeedingPassword != null) accountNeedingPassword = null else onBackClick()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = if (accountNeedingPassword == null) "Iniciar Sesión" else "Configura tu contraseña",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val pendingAccount = accountNeedingPassword
            if (pendingAccount == null) {
                LoginForm(
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess,
                    onNeedsPasswordSetup = { accountNeedingPassword = it },
                    onCreateAccountClick = onCreateAccountClick
                )
            } else {
                SetCredentialsForm(
                    username = pendingAccount,
                    onSubmit = { password ->
                        viewModel.setCredentials(pendingAccount, password) { ok ->
                            if (ok) onLoginSuccess() else accountNeedingPassword = null
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onCreateAccountClick() }
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit,
    onNeedsPasswordSetup: (String) -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<LoginResult?>(null) }
    var failedUsername by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    val usernameEmpty = username.isBlank()
    val passwordEmpty = password.isEmpty()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = PrimaryNeon
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Introduce el usuario y la contraseña de tu cuenta local para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = username,
        onValueChange = { username = it; loginError = null },
        label = { Text("Usuario") },
        singleLine = true,
        isError = (attemptedSubmit && usernameEmpty) || loginError == LoginResult.USER_NOT_FOUND,
        supportingText = {
            if (attemptedSubmit && usernameEmpty) Text("Introduce tu nombre de usuario")
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it; loginError = null },
        label = { Text("Contraseña") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        isError = (attemptedSubmit && passwordEmpty) || loginError == LoginResult.WRONG_PASSWORD,
        supportingText = {
            if (attemptedSubmit && passwordEmpty) Text("Introduce tu contraseña")
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    when (loginError) {
        LoginResult.USER_NOT_FOUND -> {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = ErrorColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "No existe ninguna cuenta con el usuario \"$failedUsername\" en este dispositivo.",
                        color = ErrorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crear una cuenta nueva",
                        color = PrimaryNeon,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onCreateAccountClick() }
                    )
                }
            }
        }
        LoginResult.WRONG_PASSWORD -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "La contraseña es incorrecta. Inténtalo de nuevo.",
                color = ErrorColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
        else -> {}
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            attemptedSubmit = true
            loginError = null
            if (!usernameEmpty && !passwordEmpty) {
                val typedUsername = username.trim()
                isChecking = true
                viewModel.login(typedUsername, password) { result ->
                    isChecking = false
                    when (result) {
                        LoginResult.SUCCESS -> onLoginSuccess()
                        LoginResult.NEEDS_PASSWORD_SETUP -> onNeedsPasswordSetup(typedUsername)
                        LoginResult.WRONG_PASSWORD -> {
                            // Se vacia la contrasena sin mostrar ademas el aviso
                            // de "campo vacio": basta con el error de contrasena.
                            password = ""
                            attemptedSubmit = false
                            loginError = result
                        }
                        LoginResult.USER_NOT_FOUND -> {
                            failedUsername = typedUsername
                            loginError = result
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = !isChecking,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SetCredentialsForm(
    username: String,
    onSubmit: (password: String) -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val pwdError = passwordError(password)
    val passwordsMatch = password == confirmPassword

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = PrimaryNeon
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "La cuenta \"$username\" se creó antes de que GameZone usara contraseñas. Crea una ahora para protegerla; tu biblioteca, valoraciones y GameZone Pro se conservan.",
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
        isError = attemptedSubmit && pwdError != null,
        supportingText = {
            if (attemptedSubmit && pwdError != null) Text(pwdError)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    PasswordRequirements(password)

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = { Text("Confirmar contraseña") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        isError = attemptedSubmit && !passwordsMatch,
        supportingText = {
            if (attemptedSubmit && !passwordsMatch) Text("Las contraseñas no coinciden")
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            attemptedSubmit = true
            if (pwdError == null && passwordsMatch) onSubmit(password)
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Guardar y continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
