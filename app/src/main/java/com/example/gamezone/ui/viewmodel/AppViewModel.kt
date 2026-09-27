package com.example.gamezone.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamezone.data.local.LoginResult
import com.example.gamezone.data.local.PaymentMethod
import com.example.gamezone.data.local.PremiumPlan
import com.example.gamezone.data.local.SubscriptionStatus
import com.example.gamezone.data.local.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    val isPremium: StateFlow<Boolean> = userPreferences.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val libraryGameIds: StateFlow<Set<Int>> = userPreferences.libraryIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val username: StateFlow<String> = userPreferences.username
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Invitado")

    // Existe una cuenta local creada (independiente de si la sesion esta activa).
    val hasProfile: StateFlow<Boolean> = userPreferences.hasProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Sesion activa en este momento. Las funciones personales (biblioteca,
    // valoraciones, cuenta) deben comprobar esto, no hasProfile.
    val isLoggedIn: StateFlow<Boolean> = userPreferences.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val avatarIndex: StateFlow<Int> = userPreferences.avatarIndex
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val userRatings: StateFlow<Map<Int, Int>> = userPreferences.userRatings
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val notificationsEnabled: StateFlow<Boolean> = userPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val premiumPlan: StateFlow<PremiumPlan> = userPreferences.premiumPlan
        .stateIn(viewModelScope, SharingStarted.Eagerly, PremiumPlan.NONE)

    val paymentMethod: StateFlow<PaymentMethod?> = userPreferences.paymentMethod
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val subscriptionStatus: StateFlow<SubscriptionStatus> = userPreferences.subscriptionStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, SubscriptionStatus.NONE)

    val renewalDateMillis: StateFlow<Long> = userPreferences.renewalDateMillis
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // Nombres de todas las cuentas locales del dispositivo (para impedir
    // nombres de usuario duplicados al crear una cuenta).
    val registeredUsernames: StateFlow<Set<String>> = userPreferences.registeredUsernames
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    // Acceso real a los beneficios Premium: exige sesion iniciada ADEMAS de tener
    // la suscripcion activa. Un invitado nunca debe ver Retro ni el estado de
    // gestion de Premium, aunque la cuenta guardada en el dispositivo sea PRO.
    val hasActivePremiumAccess: StateFlow<Boolean> = combine(isLoggedIn, isPremium) { loggedIn, premium ->
        loggedIn && premium
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Migra (si hace falta) la cuenta activa de una posible instalacion
        // anterior a este esquema de multiples cuentas, sin borrar ni resetear
        // nada. Los Flows de arriba ya funcionan igual aunque esto tarde un
        // instante, porque leen los datos legacy directamente mientras la
        // cuenta no aparezca todavia en el registro de cuentas conocidas.
        viewModelScope.launch {
            userPreferences.migrateActiveAccountIfNeeded()
        }

        // Si una cancelacion pendiente ya supero su fecha de renovacion simulada,
        // se finaliza al abrir la app: se retiran los beneficios definitivamente.
        viewModelScope.launch {
            combine(subscriptionStatus, renewalDateMillis) { status, renewal -> status to renewal }
                .collect { (status, renewal) ->
                    if (status == SubscriptionStatus.PENDING_CANCELLATION &&
                        renewal in 1..System.currentTimeMillis()
                    ) {
                        userPreferences.finalizeCancellation()
                    }
                }
        }
    }

    fun toggleLibraryGame(gameId: Int) {
        viewModelScope.launch {
            val currentIds = libraryGameIds.value
            val newIds = if (currentIds.contains(gameId)) {
                currentIds - gameId
            } else {
                currentIds + gameId
            }
            userPreferences.saveLibraryIds(newIds)
        }
    }

    // Crea una cuenta local nueva (usuario + contrasena + avatar) e inicia sesion.
    // onResult(false) si el nombre de usuario ya estaba en uso.
    fun createAccount(name: String, avatar: Int, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(userPreferences.createAccount(name, avatar, password))
        }
    }

    // Edita nombre/avatar de la cuenta ya autenticada, sin tocar credenciales.
    fun updateProfile(name: String, avatar: Int) {
        viewModelScope.launch {
            userPreferences.updateProfile(name, avatar)
        }
    }

    // Migracion: establece por primera vez la contrasena de una cuenta creada
    // antes del sistema de credenciales, sin perder biblioteca/valoraciones/Premium.
    fun setCredentials(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(userPreferences.setCredentials(username, password))
        }
    }

    // Cierra la sesion actual. La cuenta y todos sus datos (biblioteca,
    // valoraciones, Premium) permanecen intactos para la proxima vez.
    fun logout() {
        viewModelScope.launch {
            userPreferences.logout()
        }
    }

    // Intenta iniciar sesion con usuario y contrasena. NO recupera la sesion
    // automaticamente por el simple hecho de que exista una cuenta local: solo
    // si las credenciales introducidas son correctas.
    fun login(username: String, password: String, onResult: (LoginResult) -> Unit) {
        viewModelScope.launch {
            onResult(userPreferences.attemptLogin(username, password))
        }
    }

    fun setRating(gameId: Int, rating: Int) {
        viewModelScope.launch {
            userPreferences.saveRating(gameId, rating)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun isInLibrary(gameId: Int): Boolean {
        return libraryGameIds.value.contains(gameId)
    }

    // Activa GameZone Pro tras completar el resumen de pago (simulado y local).
    // Calcula una fecha de renovacion real: +30 dias (mensual) o +365 dias (anual).
    // Requiere sesion iniciada: un invitado nunca puede activar Premium.
    fun subscribe(plan: PremiumPlan, method: PaymentMethod) {
        if (!isLoggedIn.value) return
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            when (plan) {
                PremiumPlan.MONTHLY -> calendar.add(Calendar.DAY_OF_YEAR, 30)
                PremiumPlan.YEARLY -> calendar.add(Calendar.DAY_OF_YEAR, 365)
                PremiumPlan.NONE -> {}
            }
            userPreferences.activateSubscription(plan, method, calendar.timeInMillis)
        }
    }

    // El usuario solicita cancelar: conserva los beneficios hasta la fecha de renovacion.
    // Requiere sesion iniciada.
    fun requestCancellation() {
        if (!isLoggedIn.value) return
        viewModelScope.launch {
            userPreferences.requestCancellation()
        }
    }

    // Deshace una cancelacion pendiente mientras el plan sigue vigente.
    // Requiere sesion iniciada.
    fun reactivateSubscription() {
        if (!isLoggedIn.value) return
        viewModelScope.launch {
            userPreferences.reactivateSubscription()
        }
    }
}
