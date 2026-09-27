package com.example.gamezone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Resultado de un intento de inicio de sesion con usuario y contrasena.
 * SUCCESS: credenciales correctas, la sesion queda iniciada en esa cuenta.
 * USER_NOT_FOUND: no existe ninguna cuenta local con ese nombre de usuario.
 * WRONG_PASSWORD: la cuenta existe pero la contrasena no coincide.
 * NEEDS_PASSWORD_SETUP: la cuenta existe pero se creo antes del sistema de
 * credenciales y aun no tiene contrasena; debe configurarla para entrar.
 */
enum class LoginResult { SUCCESS, USER_NOT_FOUND, WRONG_PASSWORD, NEEDS_PASSWORD_SETUP }

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

/**
 * Persistencia local de cuentas GameZone.
 *
 * Cada dispositivo puede tener varias cuentas locales (usuario + contrasena).
 * Biblioteca, valoraciones, avatar, contrasena y estado de GameZone Pro son
 * datos PROPIOS de cada cuenta: se guardan en claves de DataStore cuyo nombre
 * incluye el nombre de usuario (por ejemplo "library_ids::Ana"), para que dos
 * cuentas nunca compartan ni hereden los datos de la otra.
 *
 * USERNAME, HAS_PROFILE, IS_LOGGED_IN y NOTIFICATIONS_ENABLED son, en cambio,
 * punteros/ajustes del DISPOSITIVO: indican cual es la cuenta activa ahora
 * mismo, no datos de ninguna cuenta en particular.
 *
 * Antes de este esquema, la app solo soportaba una cuenta por dispositivo y
 * guardaba estos mismos datos en claves fijas (sin nombre de usuario). Esas
 * claves se conservan aqui como LEGACY_* unicamente para migrar, la primera
 * vez que se detectan, los datos de esa cuenta antigua hacia sus nuevas claves
 * por cuenta -- sin borrarlos ni resetearlos nunca.
 */
class UserPreferences(private val context: Context) {

    companion object {
        // Punteros de sesion del dispositivo (no son datos de una cuenta).
        private val USERNAME = stringPreferencesKey("username")
        private val HAS_PROFILE = booleanPreferencesKey("has_profile")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        // Registro de las cuentas locales que ya usan el esquema por cuenta
        // (recien creadas, o cuentas antiguas ya migradas).
        private val KNOWN_ACCOUNTS = stringSetPreferencesKey("known_accounts")

        // Claves namespaced por cuenta: cada nombre de usuario tiene su propia
        // biblioteca, valoraciones, avatar, contrasena y estado Premium.
        private fun avatarKeyFor(username: String) = intPreferencesKey("avatar_index::$username")
        private fun passwordHashKeyFor(username: String) = stringPreferencesKey("password_hash::$username")
        private fun libraryKeyFor(username: String) = stringSetPreferencesKey("library_ids::$username")
        private fun ratingsKeyFor(username: String) = stringPreferencesKey("user_ratings::$username")
        private fun isPremiumKeyFor(username: String) = booleanPreferencesKey("is_premium::$username")
        private fun premiumPlanKeyFor(username: String) = stringPreferencesKey("premium_plan::$username")
        private fun paymentMethodKeyFor(username: String) = stringPreferencesKey("payment_method::$username")
        private fun subscriptionStatusKeyFor(username: String) = stringPreferencesKey("subscription_status::$username")
        private fun renewalDateKeyFor(username: String) = longPreferencesKey("renewal_date_millis::$username")

        // Claves LEGACY del esquema anterior (una sola cuenta por dispositivo).
        // Solo se leen para migrar una vez; nunca se usan para nada mas.
        private val LEGACY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val LEGACY_LIBRARY_IDS = stringSetPreferencesKey("library_ids")
        private val LEGACY_AVATAR_INDEX = intPreferencesKey("avatar_index")
        private val LEGACY_USER_RATINGS = stringPreferencesKey("user_ratings")
        private val LEGACY_PREMIUM_PLAN = stringPreferencesKey("premium_plan")
        private val LEGACY_PAYMENT_METHOD = stringPreferencesKey("payment_method")
        private val LEGACY_SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")
        private val LEGACY_RENEWAL_DATE_MILLIS = longPreferencesKey("renewal_date_millis")
        private val LEGACY_PASSWORD_HASH = stringPreferencesKey("password_hash")
    }

    private fun isKnownAccount(prefs: Preferences, username: String): Boolean =
        (prefs[KNOWN_ACCOUNTS] ?: emptySet()).contains(username)

    // Todas las cuentas locales del dispositivo: las registradas en el esquema
    // por cuenta, mas la cuenta antigua (esquema de una sola cuenta) si todavia
    // no ha sido migrada.
    private fun allAccounts(prefs: Preferences): Set<String> {
        val known = prefs[KNOWN_ACCOUNTS] ?: emptySet()
        val legacy = prefs[USERNAME]
        return if (legacy != null && prefs[HAS_PROFILE] == true && legacy !in known) known + legacy else known
    }

    // Busca una cuenta ignorando mayusculas/minusculas ("prueba123" encuentra
    // "Prueba123") y devuelve su nombre exacto, que es el que indexa sus datos.
    private fun findAccount(prefs: Preferences, username: String): String? =
        allAccounts(prefs).firstOrNull { it.equals(username, ignoreCase = true) }

    private fun storedPasswordHash(prefs: Preferences, account: String): String =
        (if (isKnownAccount(prefs, account)) prefs[passwordHashKeyFor(account)]
         else prefs[LEGACY_PASSWORD_HASH]) ?: ""

    // Copia (una sola vez) los datos de la cuenta creada antes de este esquema
    // de multiples cuentas hacia sus propias claves namespaced, identificadas
    // por su nombre de usuario. No borra ni resetea las claves legacy, y nunca
    // sobreescribe una cuenta que ya fue migrada o que ya existe bajo el nuevo
    // esquema (KNOWN_ACCOUNTS la protege de una segunda migracion).
    private fun migrateLegacyIfNeeded(prefs: MutablePreferences, username: String) {
        if (username.isEmpty()) return
        val known = prefs[KNOWN_ACCOUNTS] ?: emptySet()
        if (username in known) return

        // Solo hay datos legacy que migrar si esta es justo la cuenta que ya
        // estaba guardada bajo el esquema anterior de una sola cuenta.
        val isLegacyAccount = prefs[USERNAME] == username && prefs[HAS_PROFILE] == true
        if (isLegacyAccount) {
            prefs[LEGACY_IS_PREMIUM]?.let { prefs[isPremiumKeyFor(username)] = it }
            prefs[LEGACY_LIBRARY_IDS]?.let { prefs[libraryKeyFor(username)] = it }
            prefs[LEGACY_AVATAR_INDEX]?.let { prefs[avatarKeyFor(username)] = it }
            prefs[LEGACY_USER_RATINGS]?.let { prefs[ratingsKeyFor(username)] = it }
            prefs[LEGACY_PREMIUM_PLAN]?.let { prefs[premiumPlanKeyFor(username)] = it }
            prefs[LEGACY_PAYMENT_METHOD]?.let { prefs[paymentMethodKeyFor(username)] = it }
            prefs[LEGACY_SUBSCRIPTION_STATUS]?.let { prefs[subscriptionStatusKeyFor(username)] = it }
            prefs[LEGACY_RENEWAL_DATE_MILLIS]?.let { prefs[renewalDateKeyFor(username)] = it }
            prefs[LEGACY_PASSWORD_HASH]?.let { prefs[passwordHashKeyFor(username)] = it }
        }
        prefs[KNOWN_ACCOUNTS] = known + username
    }

    // Migra la cuenta actualmente activa (si hace falta). Se invoca una vez al
    // iniciar la app para que una cuenta antigua que ya estaba con la sesion
    // iniciada quede migrada de inmediato, sin esperar a la primera escritura.
    suspend fun migrateActiveAccountIfNeeded() {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
        }
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val u = prefs[USERNAME] ?: return@map false
        if (isKnownAccount(prefs, u)) prefs[isPremiumKeyFor(u)] ?: false
        else prefs[LEGACY_IS_PREMIUM] ?: false
    }

    val libraryIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        val u = prefs[USERNAME] ?: return@map emptySet()
        val raw = if (isKnownAccount(prefs, u)) prefs[libraryKeyFor(u)] else prefs[LEGACY_LIBRARY_IDS]
        raw?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
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
        val u = prefs[USERNAME] ?: return@map 0
        if (isKnownAccount(prefs, u)) prefs[avatarKeyFor(u)] ?: 0
        else prefs[LEGACY_AVATAR_INDEX] ?: 0
    }

    // Formato: "id:rating,id:rating"
    val userRatings: Flow<Map<Int, Int>> = context.dataStore.data.map { prefs ->
        val u = prefs[USERNAME] ?: return@map emptyMap()
        val raw = (if (isKnownAccount(prefs, u)) prefs[ratingsKeyFor(u)] else prefs[LEGACY_USER_RATINGS]) ?: ""
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
        val u = prefs[USERNAME] ?: return@map PremiumPlan.NONE
        val raw = if (isKnownAccount(prefs, u)) prefs[premiumPlanKeyFor(u)] else prefs[LEGACY_PREMIUM_PLAN]
        when (raw) {
            PremiumPlan.MONTHLY.name -> PremiumPlan.MONTHLY
            PremiumPlan.YEARLY.name -> PremiumPlan.YEARLY
            else -> PremiumPlan.NONE
        }
    }

    val paymentMethod: Flow<PaymentMethod?> = context.dataStore.data.map { prefs ->
        val u = prefs[USERNAME] ?: return@map null
        val raw = if (isKnownAccount(prefs, u)) prefs[paymentMethodKeyFor(u)] else prefs[LEGACY_PAYMENT_METHOD]
        raw?.let { r -> PaymentMethod.entries.find { it.name == r } }
    }

    val subscriptionStatus: Flow<SubscriptionStatus> = context.dataStore.data.map { prefs ->
        val u = prefs[USERNAME] ?: return@map SubscriptionStatus.NONE
        val raw = if (isKnownAccount(prefs, u)) prefs[subscriptionStatusKeyFor(u)] else prefs[LEGACY_SUBSCRIPTION_STATUS]
        when (raw) {
            SubscriptionStatus.ACTIVE.name -> SubscriptionStatus.ACTIVE
            SubscriptionStatus.PENDING_CANCELLATION.name -> SubscriptionStatus.PENDING_CANCELLATION
            SubscriptionStatus.CANCELLED.name -> SubscriptionStatus.CANCELLED
            else -> SubscriptionStatus.NONE
        }
    }

    val renewalDateMillis: Flow<Long> = context.dataStore.data.map { prefs ->
        val u = prefs[USERNAME] ?: return@map 0L
        (if (isKnownAccount(prefs, u)) prefs[renewalDateKeyFor(u)] else prefs[LEGACY_RENEWAL_DATE_MILLIS]) ?: 0L
    }

    // Nombres de todas las cuentas locales del dispositivo. Se usa al crear
    // una cuenta para impedir nombres de usuario duplicados.
    val registeredUsernames: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        allAccounts(prefs)
    }

    suspend fun saveLibraryIds(ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
            prefs[libraryKeyFor(u)] = ids.map { it.toString() }.toSet()
        }
    }

    // Crea una cuenta local nueva (usuario + contrasena + avatar) e inicia
    // sesion de inmediato. Se registra directamente en KNOWN_ACCOUNTS (sin
    // pasar por la migracion legacy) para que empiece siempre vacia: sin
    // Premium, sin biblioteca y sin valoraciones, sin importar los datos que
    // tenga cualquier otra cuenta guardada en este dispositivo.
    // Devuelve false (sin tocar nada) si ya existe una cuenta con ese nombre.
    suspend fun createAccount(name: String, avatar: Int, password: String): Boolean {
        var created = false
        context.dataStore.edit { prefs ->
            if (findAccount(prefs, name) != null) return@edit

            // Si la cuenta activa hasta ahora es una cuenta antigua aun sin
            // migrar, se migra antes de mover el puntero: sus datos legacy solo
            // son alcanzables mientras USERNAME apunte a ella.
            prefs[USERNAME]?.let { migrateLegacyIfNeeded(prefs, it) }

            val known = prefs[KNOWN_ACCOUNTS] ?: emptySet()
            prefs[KNOWN_ACCOUNTS] = known + name

            prefs[USERNAME] = name
            prefs[HAS_PROFILE] = true
            prefs[IS_LOGGED_IN] = true
            prefs[avatarKeyFor(name)] = avatar
            prefs[passwordHashKeyFor(name)] = hashPassword(password)
            created = true
        }
        return created
    }

    // Edita el avatar de la cuenta ya autenticada. El nombre de usuario no se
    // puede cambiar aqui (el campo esta bloqueado en la pantalla de edicion):
    // permitir renombrar una cuenta rompe la separacion de datos por cuenta,
    // ya que todas sus claves estan indexadas por el nombre actual.
    suspend fun updateProfile(name: String, avatar: Int) {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
            prefs[avatarKeyFor(u)] = avatar
        }
    }

    // Migracion segura para cuentas creadas antes del sistema de credenciales:
    // establece una contrasena por primera vez para la cuenta indicada (que el
    // usuario escribio en el formulario de login) sin tocar biblioteca,
    // valoraciones ni el estado de GameZone Pro, e inicia sesion en ella.
    // Solo actua si la cuenta existe y todavia no tiene contrasena.
    suspend fun setCredentials(username: String, password: String): Boolean {
        var done = false
        context.dataStore.edit { prefs ->
            val account = findAccount(prefs, username) ?: return@edit
            if (storedPasswordHash(prefs, account).isNotEmpty()) return@edit
            migrateLegacyIfNeeded(prefs, account)
            prefs[passwordHashKeyFor(account)] = hashPassword(password)
            prefs[USERNAME] = account
            prefs[IS_LOGGED_IN] = true
            done = true
        }
        return done
    }

    // Cierra sesion SIN borrar la cuenta ni ninguno de sus datos (biblioteca,
    // valoraciones, estado Premium). La cuenta sigue existiendo para volver a
    // entrar, y el puntero de usuario activo se conserva para recordar cual
    // fue la ultima cuenta usada en este dispositivo.
    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
        }
    }

    // Valida usuario y contrasena contra las cuentas locales guardadas en este
    // dispositivo (nueva cuenta namespaced, o la cuenta legacy si aun no fue
    // migrada). Si son correctas, cambia el puntero de cuenta activa a la
    // cuenta solicitada -- asi cada login recupera unicamente los datos de esa
    // cuenta. NO inicia sesion automaticamente por el simple hecho de que
    // exista un perfil: solo si las credenciales introducidas coinciden.
    suspend fun attemptLogin(username: String, password: String): LoginResult {
        val prefs = context.dataStore.data.first()
        val account = findAccount(prefs, username) ?: return LoginResult.USER_NOT_FOUND
        val storedHash = storedPasswordHash(prefs, account)

        return when {
            storedHash.isEmpty() -> LoginResult.NEEDS_PASSWORD_SETUP
            hashPassword(password) != storedHash -> LoginResult.WRONG_PASSWORD
            else -> {
                context.dataStore.edit { editPrefs ->
                    migrateLegacyIfNeeded(editPrefs, account)
                    editPrefs[USERNAME] = account
                    editPrefs[IS_LOGGED_IN] = true
                }
                LoginResult.SUCCESS
            }
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun saveRating(gameId: Int, rating: Int) {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)

            val currentRaw = prefs[ratingsKeyFor(u)] ?: ""
            val currentMap = if (currentRaw.isEmpty()) mutableMapOf()
                            else currentRaw.split(",").associate {
                                val parts = it.split(":")
                                parts[0].toInt() to parts[1].toInt()
                            }.toMutableMap()

            currentMap[gameId] = rating
            prefs[ratingsKeyFor(u)] = currentMap.map { "${it.key}:${it.value}" }.joinToString(",")
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
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
            prefs[isPremiumKeyFor(u)] = true
            prefs[premiumPlanKeyFor(u)] = plan.name
            prefs[paymentMethodKeyFor(u)] = method.name
            prefs[subscriptionStatusKeyFor(u)] = SubscriptionStatus.ACTIVE.name
            prefs[renewalDateKeyFor(u)] = renewalDateMillis
        }
    }

    // El usuario cancela: conserva los beneficios (isPremium sigue true) hasta la
    // fecha de renovacion ya guardada. El estado pasa a "pendiente de cancelacion".
    suspend fun requestCancellation() {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
            prefs[subscriptionStatusKeyFor(u)] = SubscriptionStatus.PENDING_CANCELLATION.name
        }
    }

    // Deshace una cancelacion pendiente mientras aun no llega la fecha de renovacion.
    // La confirmacion previa a esta accion vive en la UI (PremiumScreen); esta
    // funcion sigue haciendo exactamente lo mismo que antes.
    suspend fun reactivateSubscription() {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
            prefs[subscriptionStatusKeyFor(u)] = SubscriptionStatus.ACTIVE.name
        }
    }

    // Se llama cuando la fecha de renovacion simulada ya paso y la cancelacion
    // estaba pendiente: ahora si se retiran los beneficios definitivamente.
    suspend fun finalizeCancellation() {
        context.dataStore.edit { prefs ->
            val u = prefs[USERNAME] ?: return@edit
            migrateLegacyIfNeeded(prefs, u)
            prefs[isPremiumKeyFor(u)] = false
            prefs[subscriptionStatusKeyFor(u)] = SubscriptionStatus.CANCELLED.name
            prefs[premiumPlanKeyFor(u)] = PremiumPlan.NONE.name
        }
    }
}
