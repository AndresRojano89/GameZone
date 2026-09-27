package com.example.gamezone.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PrimaryVariant
import com.example.gamezone.ui.viewmodel.AppViewModel

// Reglas de cuenta compartidas por Crear cuenta y por el login (configurar
// contrasena de una cuenta antigua), para que ambas validen exactamente igual.
internal const val MIN_USERNAME_LENGTH = 3
internal const val MAX_USERNAME_LENGTH = 15
internal const val MIN_PASSWORD_LENGTH = 6

// Devuelve el error del nombre de usuario (ya recortado) o null si es valido.
// La comparacion con las cuentas existentes ignora mayusculas/minusculas.
internal fun usernameError(rawName: String, takenNames: Set<String>): String? {
    val name = rawName.trim()
    return when {
        rawName.isEmpty() -> "Introduce un nombre de usuario"
        name.isEmpty() -> "El nombre no puede contener solo espacios"
        name.length < MIN_USERNAME_LENGTH -> "El nombre debe tener al menos $MIN_USERNAME_LENGTH caracteres"
        name.length > MAX_USERNAME_LENGTH -> "El nombre puede tener como máximo $MAX_USERNAME_LENGTH caracteres"
        takenNames.any { it.equals(name, ignoreCase = true) } -> "El usuario \"$name\" ya existe en este dispositivo"
        else -> null
    }
}

internal fun passwordHasMinLength(password: String) = password.length >= MIN_PASSWORD_LENGTH
internal fun passwordHasLetter(password: String) = password.any { it.isLetter() }
internal fun passwordHasDigit(password: String) = password.any { it.isDigit() }

// Devuelve el error de la contrasena o null si es valida.
internal fun passwordError(password: String): String? = when {
    password.isEmpty() -> "Introduce una contraseña"
    password.isBlank() -> "La contraseña no puede contener solo espacios"
    !passwordHasMinLength(password) -> "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
    !passwordHasLetter(password) -> "La contraseña debe contener al menos una letra"
    !passwordHasDigit(password) -> "La contraseña debe contener al menos un número"
    else -> null
}

// Avatares disponibles: mismos degradados que ya usaba GameZone (el indice es
// lo que se guarda por cuenta), ahora con un nombre para mostrarlo en el resumen.
internal data class AvatarOption(val name: String, val colors: List<Color>)

internal val avatarOptions = listOf(
    AvatarOption("Neón violeta", listOf(PrimaryNeon, PrimaryVariant)),
    AvatarOption("Turquesa", listOf(Color(0xFF03DAC6), Color(0xFF018786))),
    AvatarOption("Carmesí", listOf(Color(0xFFF44336), Color(0xFFB71C1C))),
    AvatarOption("Dorado", listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D))),
    AvatarOption("Azul océano", listOf(Color(0xFF2196F3), Color(0xFF0D47A1)))
)

@Composable
internal fun accountFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryNeon,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = PrimaryNeon,
    unfocusedLabelColor = Color.Gray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledBorderColor = Color.Gray.copy(alpha = 0.4f),
    disabledLabelColor = Color.Gray,
    disabledTextColor = Color.White
)

private enum class CreateAccountStep(val number: Int, val title: String) {
    ACCOUNT(1, "Datos de cuenta"),
    PERSONALIZATION(2, "Personalización"),
    CONFIRMATION(3, "Confirmación")
}

@Composable
fun CreateProfileScreen(
    onBackClick: () -> Unit,
    // Se dispara al guardar con éxito la edición del perfil.
    onAccountSaved: () -> Unit = onBackClick,
    // Se dispara cuando la cuenta nueva ya fue creada y la sesión iniciada.
    onAccountCreated: () -> Unit = onAccountSaved,
    viewModel: AppViewModel
) {
    // Se decide una sola vez al entrar: si ya hay sesion se edita el perfil; si
    // no, siempre se crea una cuenta nueva. Se fija con remember para que, al
    // crear la cuenta (que inicia sesion), la pantalla no cambie de modo antes
    // de navegar.
    val isEditMode = remember { viewModel.isLoggedIn.value }

    if (isEditMode) {
        EditProfileContent(onBackClick = onBackClick, onSaved = onAccountSaved, viewModel = viewModel)
    } else {
        CreateAccountWizard(onBackClick = onBackClick, onCreated = onAccountCreated, viewModel = viewModel)
    }
}

// ---------------------------------------------------------------------------
// Editar perfil (sesion iniciada): solo avatar; el usuario identifica la cuenta.
// ---------------------------------------------------------------------------

@Composable
private fun EditProfileContent(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AppViewModel
) {
    val currentUsername by viewModel.username.collectAsState()
    val currentAvatarIndex by viewModel.avatarIndex.collectAsState()
    var selectedAvatar by rememberSaveable { mutableIntStateOf(currentAvatarIndex.coerceIn(avatarOptions.indices)) }

    AccountScreenScaffold(title = "Editar Perfil", onBackClick = onBackClick) {
        Spacer(modifier = Modifier.height(24.dp))
        AvatarPreview(selectedAvatar)
        Spacer(modifier = Modifier.height(32.dp))
        AvatarPicker(selected = selectedAvatar, onSelect = { selectedAvatar = it })
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = currentUsername,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de usuario") },
            singleLine = true,
            // El nombre de usuario identifica la cuenta y separa sus datos
            // (biblioteca, valoraciones, Premium) del resto de cuentas del
            // dispositivo, asi que no se puede renombrar al editar el perfil.
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = accountFieldColors()
        )
        Text(
            text = "El nombre de usuario no se puede cambiar.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        PrimaryActionButton(text = "Guardar Cambios") {
            viewModel.updateProfile(currentUsername, selectedAvatar)
            onSaved()
        }
    }
}

// ---------------------------------------------------------------------------
// Crear cuenta: Datos de cuenta -> Personalizacion -> Confirmacion.
// ---------------------------------------------------------------------------

@Composable
private fun CreateAccountWizard(
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AppViewModel
) {
    val takenNames by viewModel.registeredUsernames.collectAsState()

    var step by rememberSaveable { mutableStateOf(CreateAccountStep.ACCOUNT) }
    var name by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var selectedAvatar by rememberSaveable { mutableIntStateOf(0) }
    var isCreating by remember { mutableStateOf(false) }

    val goBack: () -> Unit = {
        when (step) {
            CreateAccountStep.ACCOUNT -> onBackClick()
            CreateAccountStep.PERSONALIZATION -> step = CreateAccountStep.ACCOUNT
            CreateAccountStep.CONFIRMATION -> step = CreateAccountStep.PERSONALIZATION
        }
    }
    // El boton "Atras" del sistema retrocede un paso en lugar de abandonar el registro.
    BackHandler(enabled = step != CreateAccountStep.ACCOUNT && !isCreating) { goBack() }

    AccountScreenScaffold(title = "Crear cuenta", onBackClick = { if (!isCreating) goBack() }) {
        StepIndicator(step)
        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            CreateAccountStep.ACCOUNT -> AccountDataStep(
                name = name,
                onNameChange = { if (it.length <= MAX_USERNAME_LENGTH) name = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                takenNames = takenNames,
                onContinue = {
                    name = name.trim()
                    step = CreateAccountStep.PERSONALIZATION
                }
            )
            CreateAccountStep.PERSONALIZATION -> PersonalizationStep(
                username = name.trim(),
                selectedAvatar = selectedAvatar,
                onSelectAvatar = { selectedAvatar = it },
                onContinue = { step = CreateAccountStep.CONFIRMATION }
            )
            CreateAccountStep.CONFIRMATION -> ConfirmationStep(
                username = name.trim(),
                avatarIndex = selectedAvatar,
                isCreating = isCreating,
                onEdit = { step = CreateAccountStep.PERSONALIZATION },
                onCreate = {
                    isCreating = true
                    viewModel.createAccount(name.trim(), selectedAvatar, password) { created ->
                        isCreating = false
                        if (created) {
                            onCreated()
                        } else {
                            // Otra cuenta con ese nombre se creo entre medias:
                            // se vuelve al paso 1, donde se mostrara el error.
                            step = CreateAccountStep.ACCOUNT
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun StepIndicator(step: CreateAccountStep) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Paso ${step.number} de ${CreateAccountStep.entries.size} · ${step.title}",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryNeon,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            CreateAccountStep.entries.forEach { s ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (s.number <= step.number) PrimaryNeon else Color.Gray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun AccountDataStep(
    name: String,
    onNameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    takenNames: Set<String>,
    onContinue: () -> Unit
) {
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val nameError = usernameError(name, takenNames)
    val pwdError = passwordError(password)
    val confirmError = when {
        confirmPassword.isEmpty() -> "Confirma tu contraseña"
        confirmPassword != password -> "Las contraseñas no coinciden"
        else -> null
    }

    // Los errores se muestran mientras se escribe o tras pulsar "Continuar",
    // pero no sobre un campo vacio que el usuario aun no ha tocado.
    val showNameError = nameError != null && (attemptedSubmit || name.isNotEmpty())
    val showPwdError = pwdError != null && attemptedSubmit
    val showConfirmError = confirmError != null && (attemptedSubmit || confirmPassword.isNotEmpty())

    Text(
        text = "Crea las credenciales con las que iniciarás sesión en este dispositivo.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Nombre de usuario") },
        placeholder = { Text("Escribe tu nombre...") },
        singleLine = true,
        isError = showNameError,
        supportingText = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (showNameError) nameError.orEmpty() else "Entre $MIN_USERNAME_LENGTH y $MAX_USERNAME_LENGTH caracteres")
                Text("${name.length}/$MAX_USERNAME_LENGTH")
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Contraseña") },
        singleLine = true,
        isError = showPwdError,
        visualTransformation = PasswordVisualTransformation(),
        supportingText = { if (showPwdError) Text(pwdError.orEmpty()) },
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    PasswordRequirements(password)

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Confirmar contraseña") },
        singleLine = true,
        isError = showConfirmError,
        visualTransformation = PasswordVisualTransformation(),
        supportingText = {
            when {
                showConfirmError -> Text(confirmError.orEmpty())
                confirmPassword.isNotEmpty() -> Text("Las contraseñas coinciden", color = PrimaryNeon)
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = accountFieldColors()
    )

    Spacer(modifier = Modifier.height(32.dp))

    PrimaryActionButton(text = "Continuar") {
        attemptedSubmit = true
        if (nameError == null && pwdError == null && confirmError == null) onContinue()
    }
}

// Lista de requisitos que se marca en vivo mientras se escribe la contrasena.
@Composable
internal fun PasswordRequirements(password: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
        RequirementRow("Al menos $MIN_PASSWORD_LENGTH caracteres", passwordHasMinLength(password))
        RequirementRow("Al menos una letra", passwordHasLetter(password))
        RequirementRow("Al menos un número", passwordHasDigit(password))
    }
}

@Composable
private fun RequirementRow(text: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) PrimaryNeon else Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (met) Color.White else Color.Gray)
    }
}

@Composable
private fun PersonalizationStep(
    username: String,
    selectedAvatar: Int,
    onSelectAvatar: (Int) -> Unit,
    onContinue: () -> Unit
) {
    AvatarPreview(selectedAvatar)

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = username,
        style = MaterialTheme.typography.headlineSmall,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Nombre de usuario",
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray
    )

    Spacer(modifier = Modifier.height(32.dp))

    AvatarPicker(selected = selectedAvatar, onSelect = onSelectAvatar)

    Spacer(modifier = Modifier.height(40.dp))

    PrimaryActionButton(text = "Continuar", onClick = onContinue)
}

@Composable
private fun ConfirmationStep(
    username: String,
    avatarIndex: Int,
    isCreating: Boolean,
    onEdit: () -> Unit,
    onCreate: () -> Unit
) {
    Text(
        text = "Crear cuenta",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "Revisa los datos antes de crear tu cuenta.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPreview(avatarIndex, size = 56)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(username, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Cuenta local · Gratuita", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            SummaryRow(label = "Usuario", value = username)
            SummaryRow(label = "Avatar", value = avatarOptions[avatarIndex.coerceIn(avatarOptions.indices)].name)
            SummaryRow(label = "Contraseña", value = "••••••••")
            SummaryRow(label = "Plan", value = "Gratuito")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Tu biblioteca, valoraciones y progreso se almacenarán en este dispositivo y estarán asociados a esta cuenta.",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(32.dp))

    PrimaryActionButton(text = if (isCreating) "Creando cuenta..." else "Crear cuenta", enabled = !isCreating, onClick = onCreate)

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = onEdit,
        enabled = !isCreating,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Volver a editar", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------------------------------------------------------------------
// Componentes comunes de las pantallas de cuenta.
// ---------------------------------------------------------------------------

@Composable
private fun AccountScreenScaffold(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AvatarPreview(avatarIndex: Int, size: Int = 120) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                brush = Brush.linearGradient(avatarOptions[avatarIndex.coerceIn(avatarOptions.indices)].colors),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size((size / 2).dp),
            tint = Color.Black
        )
    }
}

@Composable
private fun AvatarPicker(selected: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Personaliza tu identidad visual",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Elige el avatar y el color que te representarán.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(avatarOptions) { index, option ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(brush = Brush.linearGradient(option.colors))
                            .border(
                                width = if (selected == index) 3.dp else 0.dp,
                                color = if (selected == index) Color.White else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected == index) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected == index) Color.White else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryNeon,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
