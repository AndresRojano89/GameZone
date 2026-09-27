package com.example.gamezone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Estados posibles de la suscripcion GameZone Pro.
 * NONE: nunca se ha suscrito.
 * ACTIVE: suscripcion activa y vigente.
 * PENDING_CANCELLATION: el usuario cancelo, pero conserva los beneficios hasta renewalDateMillis.
 * CANCELLED: la suscripcion ya finalizo (fecha simulada superada).
 */
enum class SubscriptionStatus { NONE, ACTIVE, PENDING_CANCELLATION, CANCELLED }

enum class PremiumPlan(val label: String) {
    NONE(""),
    MONTHLY("Mensual"),
    YEARLY("Anual")
}

enum class PaymentMethod(val label: String) {
    CARD("Tarjeta"),
    NEQUI("Nequi"),
    PSE("PSE")
}

class UserPreferences(private val context: Context) {

    companion object {
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val LIBRARY_IDS = stringSetPreferencesKey("library_ids")
        private val USERNAME = stringPreferencesKey("username")
        private val HAS_PROFILE = booleanPreferencesKey("has_profile")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val AVATAR_INDEX = intPreferencesKey("avatar_index")
        private val USER_RATINGS = stringPreferencesKey("user_ratings")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val PREMIUM_PLAN = stringPreferencesKey("premium_plan")
        private val PAYMENT_METHOD = stringPreferencesKey("payment_method")
        private val SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")
        private val RENEWAL_DATE_MILLIS = longPreferencesKey("renewal_date_millis")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PREMIUM] ?: false
    }

    val libraryIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[LIBRARY_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    val username: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USERNAME] ?: "Invitado"
    }

    // Indica si existe una cuenta local creada alguna vez (persiste entre sesiones).
    val hasProfile: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_PROFILE] ?: false
    }

    // Indica si la sesion esta activa en este momento. Distinto de hasProfile:
    // cerrar sesion pone esto en false pero NUNCA borra la cuenta ni sus datos.
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    val avatarIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AVATAR_INDEX] ?: 0
    }

    // Formato: "id:rating,id:rating"
    val userRatings: Flow<Map<Int, Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[USER_RATINGS] ?: ""
        if (raw.isEmpty()) return@map emptyMap()
        raw.split(",").associate {
            val parts = it.split(":")
            parts[0].toInt() to parts[1].toInt()
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    val premiumPlan: Flow<PremiumPlan> = context.dataStore.data.map { prefs ->
        when (prefs[PREMIUM_PLAN]) {
            PremiumPlan.MONTHLY.name -> PremiumPlan.MONTHLY
            PremiumPlan.YEARLY.name -> PremiumPlan.YEARLY
            else -> PremiumPlan.NONE
        }
    }

    val paymentMethod: Flow<PaymentMethod?> = context.dataStore.data.map { prefs ->
        prefs[PAYMENT_METHOD]?.let { raw -> PaymentMethod.entries.find { it.name == raw } }
    }

    val subscriptionStatus: Flow<SubscriptionStatus> = context.dataStore.data.map { prefs ->
        when (prefs[SUBSCRIPTION_STATUS]) {
            SubscriptionStatus.ACTIVE.name -> SubscriptionStatus.ACTIVE
            SubscriptionStatus.PENDING_CANCELLATION.name -> SubscriptionStatus.PENDING_CANCELLATION
            SubscriptionStatus.CANCELLED.name -> SubscriptionStatus.CANCELLED
            else -> SubscriptionStatus.NONE
        }
    }

    val renewalDateMillis: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[RENEWAL_DATE_MILLIS] ?: 0L
    }

    suspend fun saveLibraryIds(ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[LIBRARY_IDS] = ids.map { it.toString() }.toSet()
        }
    }

    // Crea o edita la cuenta local. Al crearla por primera vez tambien inicia sesion.
    suspend fun setProfileInfo(name: String, avatar: Int) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME] = name
            prefs[AVATAR_INDEX] = avatar
            prefs[HAS_PROFILE] = true
            prefs[IS_LOGGED_IN] = true
        }
    }

    // Cierra sesion SIN borrar la cuenta ni ninguno de sus datos (biblioteca,
    // valoraciones, estado Premium). La cuenta sigue existiendo para volver a entrar.
    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
        }
    }

    // Vuelve a activar la sesion de la cuenta local existente, sin pedir datos de nuevo.
    suspend fun login() {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
        }
    }

    suspend fun saveRating(gameId: Int, rating: Int) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[USER_RATINGS] ?: ""
            val currentMap = if (currentRaw.isEmpty()) mutableMapOf()
                            else currentRaw.split(",").associate {
                                val parts = it.split(":")
                                parts[0].toInt() to parts[1].toInt()
                            }.toMutableMap()

            currentMap[gameId] = rating
            prefs[USER_RATINGS] = currentMap.map { "${it.key}:${it.value}" }.joinToString(",")
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Activa GameZone Pro con un plan y metodo de pago concretos (simulados),
    // y calcula una fecha de renovacion real a partir de la fecha del dispositivo.
    suspend fun activateSubscription(plan: PremiumPlan, method: PaymentMethod, renewalDateMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[IS_PREMIUM] = true
            prefs[PREMIUM_PLAN] = plan.name
            prefs[PAYMENT_METHOD] = method.name
            prefs[SUBSCRIPTION_STATUS] = SubscriptionStatus.ACTIVE.name
            prefs[RENEWAL_DATE_MILLIS] = renewalDateMillis
        }
    }

    // El usuario cancela: conserva los beneficios (isPremium sigue true) hasta la
    // fecha de renovacion ya guardada. El estado pasa a "pendiente de cancelacion".
    suspend fun requestCancellation() {
        context.dataStore.edit { prefs ->
            prefs[SUBSCRIPTION_STATUS] = SubscriptionStatus.PENDING_CANCELLATION.name
        }
    }

    // Deshace una cancelacion pendiente mientras aun no llega la fecha de renovacion.
    suspend fun reactivateSubscription() {
        context.dataStore.edit { prefs ->
            prefs[SUBSCRIPTION_STATUS] = SubscriptionStatus.ACTIVE.name
        }
    }

    // Se llama cuando la fecha de renovacion simulada ya paso y la cancelacion
    // estaba pendiente: ahora si se retiran los beneficios definitivamente.
    suspend fun finalizeCancellation() {
        context.dataStore.edit { prefs ->
            prefs[IS_PREMIUM] = false
            prefs[SUBSCRIPTION_STATUS] = SubscriptionStatus.CANCELLED.name
            prefs[PREMIUM_PLAN] = PremiumPlan.NONE.name
        }
    }
}
