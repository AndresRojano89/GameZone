package com.example.gamezone.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    init {
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

    fun createProfile(name: String, avatar: Int) {
        viewModelScope.launch {
            userPreferences.setProfileInfo(name, avatar)
        }
    }

    // Cierra la sesion actual. La cuenta y todos sus datos (biblioteca,
    // valoraciones, Premium) permanecen intactos para la proxima vez.
    fun logout() {
        viewModelScope.launch {
            userPreferences.logout()
        }
    }

    // Reactiva la sesion de la cuenta local ya existente, sin volver a pedir datos.
    fun login() {
        viewModelScope.launch {
            userPreferences.login()
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
    fun subscribe(plan: PremiumPlan, method: PaymentMethod) {
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
    fun requestCancellation() {
        viewModelScope.launch {
            userPreferences.requestCancellation()
        }
    }

    // Deshace una cancelacion pendiente mientras el plan sigue vigente.
    fun reactivateSubscription() {
        viewModelScope.launch {
            userPreferences.reactivateSubscription()
        }
    }
}
