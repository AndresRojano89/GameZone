package com.example.gamezone.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gamezone.data.local.PaymentMethod
import com.example.gamezone.data.local.PremiumPlan
import com.example.gamezone.data.local.SubscriptionStatus
import com.example.gamezone.ui.components.GameZoneLogo
import com.example.gamezone.ui.components.PremiumBadge
import com.example.gamezone.ui.theme.PrimaryNeon
import com.example.gamezone.ui.theme.PrimaryVariant
import com.example.gamezone.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SubscribeStep { PAYMENT_METHOD, PAYMENT_DETAILS, SUMMARY, SUCCESS }

private fun planPrice(plan: PremiumPlan): String = when (plan) {
    PremiumPlan.MONTHLY -> "$4.99"
    PremiumPlan.YEARLY -> "$39.99"
    PremiumPlan.NONE -> ""
}

private fun planPeriod(plan: PremiumPlan): String = when (plan) {
    PremiumPlan.MONTHLY -> "/mes"
    PremiumPlan.YEARLY -> "/año"
    PremiumPlan.NONE -> ""
}

private fun formatDate(millis: Long): String {
    if (millis <= 0L) return "-"
    val formatter = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
    return formatter.format(Date(millis))
}

@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    onCreateProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AppViewModel
) {
    // Acceso real a beneficios/gestion Premium: exige sesion iniciada ademas de
    // la suscripcion. Un invitado siempre ve la pantalla como promocion (planes
    // para suscribirse), nunca la gestion de una suscripcion de otra sesion.
    val isPremium by viewModel.hasActivePremiumAccess.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentPlan by viewModel.premiumPlan.collectAsState()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()
    val renewalDateMillis by viewModel.renewalDateMillis.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    // Confirmacion previa a reactivar una suscripcion cancelada/en periodo de
    // gracia: el boton "Reactivar Suscripción" de la pantalla principal ya no
    // reactiva de inmediato, solo abre este dialogo.
    var showReactivateDialog by remember { mutableStateOf(false) }

    // Estado del asistente de suscripción (null = no está suscribiéndose ahora mismo)
    var subscribeStep by remember { mutableStateOf<SubscribeStep?>(null) }
    var selectedPlan by remember { mutableStateOf(PremiumPlan.MONTHLY) }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    LaunchedEffect(Unit) { isVisible = true }

    fun resetWizard() {
        subscribeStep = null
        selectedMethod = null
    }

    fun startSubscribe(plan: PremiumPlan) {
        if (!isLoggedIn) {
            showLoginRequiredDialog = true
            return
        }
        selectedPlan = plan
        subscribeStep = SubscribeStep.PAYMENT_METHOD
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(targetState = subscribeStep, label = "premium_wizard") { step ->
            when (step) {
                null -> PremiumOverview(
                    isVisible = isVisible,
                    isPremium = isPremium,
                    currentPlan = currentPlan,
                    subscriptionStatus = subscriptionStatus,
                    renewalDateMillis = renewalDateMillis,
                    onBackClick = onBackClick,
                    onSelectPlan = { startSubscribe(it) },
                    onManageClick = { showCancelDialog = true },
                    onReactivateClick = { showReactivateDialog = true }
                )
                SubscribeStep.PAYMENT_METHOD -> PaymentMethodStep(
                    plan = selectedPlan,
                    onBackClick = { resetWizard() },
                    onMethodSelected = {
                        selectedMethod = it
                        subscribeStep = SubscribeStep.PAYMENT_DETAILS
                    }
                )
                SubscribeStep.PAYMENT_DETAILS -> selectedMethod?.let { method ->
                    PaymentDetailsStep(
                        plan = selectedPlan,
                        method = method,
                        onBackClick = { subscribeStep = SubscribeStep.PAYMENT_METHOD },
                        onValidated = { subscribeStep = SubscribeStep.SUMMARY }
                    )
                }
                SubscribeStep.SUMMARY -> selectedMethod?.let { method ->
                    SummaryStep(
                        plan = selectedPlan,
                        method = method,
                        onBackClick = { subscribeStep = SubscribeStep.PAYMENT_DETAILS },
                        onConfirm = {
                            viewModel.subscribe(selectedPlan, method)
                            subscribeStep = SubscribeStep.SUCCESS
                        }
                    )
                }
                SubscribeStep.SUCCESS -> SuccessStep(
                    plan = selectedPlan,
                    onDone = {
                        resetWizard()
                        onBackClick()
                    }
                )
            }
        }
    }

    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = { Text("GameZone Pro requiere una cuenta") },
            text = {
                Column {
                    Text("Crea una cuenta o inicia sesión para disfrutar GameZone Pro.")
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showLoginRequiredDialog = false
                            onCreateProfileClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black)
                    ) {
                        Text("Crear cuenta")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            showLoginRequiredDialog = false
                            onLoginClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Iniciar sesión")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLoginRequiredDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showCancelDialog) {
        val isPending = subscriptionStatus == SubscriptionStatus.PENDING_CANCELLATION
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(if (isPending) "Cancelación en curso" else "¿Cancelar Suscripción?") },
            text = {
                Column {
                    Text("Plan actual: ${currentPlan.label}", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isPending)
                            "Conservarás tus beneficios Pro hasta el ${formatDate(renewalDateMillis)}. Después de esa fecha, tu cuenta pasará al plan gratuito."
                        else
                            "Conservarás la colección Retro y la insignia PRO hasta el ${formatDate(renewalDateMillis)}, fecha de tu próxima renovación. Después de eso perderás el acceso. Puedes volver a unirte cuando quieras."
                    )
                }
            },
            confirmButton = {
                if (isPending) {
                    Button(
                        onClick = {
                            viewModel.reactivateSubscription()
                            showCancelDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black)
                    ) {
                        Text("Reactivar Suscripción")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.requestCancellation()
                            showCancelDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679), contentColor = Color.White)
                    ) {
                        Text("Confirmar Cancelación")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(if (isPending) "Cerrar" else "Mantener Pro", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showReactivateDialog) {
        AlertDialog(
            onDismissRequest = { showReactivateDialog = false },
            title = { Text("¿Reactivar suscripción?") },
            text = {
                Text(
                    "Tu suscripción GameZone Pro volverá a estar activa y se renovará " +
                        "de nuevo automáticamente en la fecha ya programada."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reactivateSubscription()
                        showReactivateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black)
                ) {
                    Text("Reactivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReactivateDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
private fun PremiumOverview(
    isVisible: Boolean,
    isPremium: Boolean,
    currentPlan: PremiumPlan,
    subscriptionStatus: SubscriptionStatus,
    renewalDateMillis: Long,
    onBackClick: () -> Unit,
    onSelectPlan: (PremiumPlan) -> Unit,
    onManageClick: () -> Unit,
    onReactivateClick: () -> Unit
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
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(100.dp), tint = PrimaryNeon.copy(alpha = 0.1f))
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(60.dp), tint = PrimaryNeon)
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

                BenefitCard {
                    BenefitItem(Icons.Default.CheckCircle, "Sin Publicidad", "Navega por todo el catálogo sin banners de anuncios.")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                    BenefitItem(Icons.Default.CheckCircle, "Colección Retro", "Acceso desbloqueado a los clásicos más legendarios.")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
                    BenefitItem(Icons.Default.CheckCircle, "Insignia Exclusiva", "Destaca tu perfil con el sello PRO oficial.")
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (!isPremium) {
                    PlanCard(
                        title = "PLAN ANUAL",
                        price = "$39.99",
                        period = "/año",
                        savings = "Ahorra 33%",
                        onClick = { onSelectPlan(PremiumPlan.YEARLY) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PlanCard(
                        title = "PLAN MENSUAL",
                        price = "$4.99",
                        period = "/mes",
                        isRecommended = true,
                        onClick = { onSelectPlan(PremiumPlan.MONTHLY) }
                    )
                } else {
                    val isPending = subscriptionStatus == SubscriptionStatus.PENDING_CANCELLATION
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
                                text = "Plan actual: ${currentPlan.label}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPending) "Estado: Cancelación en curso" else "Estado: Suscripción Activa",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isPending) Color(0xFFCF6679) else PrimaryNeon,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPending)
                                    "Beneficios activos hasta el ${formatDate(renewalDateMillis)}"
                                else "Se renueva el ${formatDate(renewalDateMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            if (isPending) {
                                Button(
                                    onClick = onReactivateClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reactivar Suscripción", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onManageClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Gestionar / Cancelar Suscripción")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun WizardScaffold(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
        content()
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PaymentMethodStep(
    plan: PremiumPlan,
    onBackClick: () -> Unit,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    WizardScaffold(title = "Método de pago", onBackClick = onBackClick) {
        Text(
            text = "Plan seleccionado: ${plan.label} · ${planPrice(plan)}${planPeriod(plan)}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        PaymentMethod.entries.forEach { method ->
            Card(
                onClick = { onMethodSelected(method) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = PrimaryNeon)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(method.label, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailsStep(
    plan: PremiumPlan,
    method: PaymentMethod,
    onBackClick: () -> Unit,
    onValidated: () -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var nequiPhone by remember { mutableStateOf("") }
    var pseDocument by remember { mutableStateOf("") }
    val pseBanks = listOf("Bancolombia", "Davivienda", "BBVA Colombia", "Banco de Bogotá")
    var pseBank by remember { mutableStateOf(pseBanks.first()) }
    var pseBankExpanded by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    val cardValid = cardName.trim().length >= 3 &&
        cardNumber.length == 16 && cardNumber.all { it.isDigit() } &&
        Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(cardExpiry) &&
        cardCvv.length == 3 && cardCvv.all { it.isDigit() }
    val nequiValid = nequiPhone.length == 10 && nequiPhone.all { it.isDigit() } && nequiPhone.startsWith("3")
    val pseValid = pseDocument.length >= 6 && pseDocument.all { it.isDigit() }

    val isValid = when (method) {
        PaymentMethod.CARD -> cardValid
        PaymentMethod.NEQUI -> nequiValid
        PaymentMethod.PSE -> pseValid
    }

    WizardScaffold(title = "Datos de ${method.label}", onBackClick = onBackClick) {
        Text(
            text = "Plan ${plan.label} · ${planPrice(plan)}${planPeriod(plan)}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (method) {
            PaymentMethod.CARD -> {
                DemoTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = "Nombre del titular",
                    error = if (attemptedSubmit && cardName.trim().length < 3) "Ingresa el nombre del titular" else null
                )
                DemoTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) cardNumber = it },
                    label = "Número de tarjeta (16 dígitos)",
                    keyboardType = KeyboardType.Number,
                    error = if (attemptedSubmit && !(cardNumber.length == 16 && cardNumber.all { it.isDigit() })) "Debe tener 16 dígitos" else null
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DemoTextField(
                            value = cardExpiry,
                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                            label = "MM/AA",
                            error = if (attemptedSubmit && !Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(cardExpiry)) "Formato MM/AA" else null
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        DemoTextField(
                            value = cardCvv,
                            onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) cardCvv = it },
                            label = "CVV",
                            keyboardType = KeyboardType.Number,
                            error = if (attemptedSubmit && !(cardCvv.length == 3 && cardCvv.all { it.isDigit() })) "3 dígitos" else null
                        )
                    }
                }
            }
            PaymentMethod.NEQUI -> {
                DemoTextField(
                    value = nequiPhone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) nequiPhone = it },
                    label = "Número de celular (Nequi)",
                    keyboardType = KeyboardType.Number,
                    error = if (attemptedSubmit && !nequiValid) "Ingresa un celular colombiano válido (10 dígitos, inicia en 3)" else null
                )
            }
            PaymentMethod.PSE -> {
                ExposedDropdownMenuBox(
                    expanded = pseBankExpanded,
                    onExpandedChange = { pseBankExpanded = it }
                ) {
                    OutlinedTextField(
                        value = pseBank,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Banco") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = demoFieldColors()
                    )
                    ExposedDropdownMenu(expanded = pseBankExpanded, onDismissRequest = { pseBankExpanded = false }) {
                        pseBanks.forEach { bank ->
                            DropdownMenuItem(text = { Text(bank) }, onClick = { pseBank = bank; pseBankExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                DemoTextField(
                    value = pseDocument,
                    onValueChange = { if (it.length <= 12 && it.all { c -> c.isDigit() }) pseDocument = it },
                    label = "Número de documento",
                    keyboardType = KeyboardType.Number,
                    error = if (attemptedSubmit && !pseValid) "Ingresa un número de documento válido" else null
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                attemptedSubmit = true
                if (isValid) onValidated()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun demoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryNeon,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = PrimaryNeon,
    unfocusedLabelColor = Color.Gray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorBorderColor = Color(0xFFCF6679),
    errorLabelColor = Color(0xFFCF6679)
)

@Composable
private fun DemoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = demoFieldColors()
        )
        if (error != null) {
            Text(error, color = Color(0xFFCF6679), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

@Composable
private fun SummaryStep(
    plan: PremiumPlan,
    method: PaymentMethod,
    onBackClick: () -> Unit,
    onConfirm: () -> Unit
) {
    WizardScaffold(title = "Resumen de tu suscripción", onBackClick = onBackClick) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SummaryRow("Plan", plan.label)
                SummaryRow("Precio", "${planPrice(plan)}${planPeriod(plan)}")
                SummaryRow("Método de pago", method.label)
                SummaryRow("Renovación", if (plan == PremiumPlan.MONTHLY) "Automática cada mes" else "Automática cada año")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Al confirmar, activarás GameZone Pro de inmediato con los beneficios de tu plan.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirmar Suscripción", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SuccessStep(plan: PremiumPlan, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(96.dp), tint = PrimaryNeon)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Ya eres GameZone PRO!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tu plan ${plan.label} está activo. Ya puedes disfrutar de la Colección Retro y tu insignia PRO.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ir a mi Perfil", fontWeight = FontWeight.Bold)
        }
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
