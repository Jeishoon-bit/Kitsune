package app.kitsune.stream.data.provider

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.providerDataStore by preferencesDataStore(name = "providers")

class ProviderStore(private val context: Context) {

    private val enabledKey = stringSetPreferencesKey("enabled_providers")

    private fun defaultIds(): Set<String> = Providers.all.filter { it.isCore }.map { it.id }.toSet()

    val enabledIds: Flow<Set<String>> = context.providerDataStore.data.map { prefs ->
        prefs[enabledKey] ?: defaultIds()
    }

    /** Devuelve false si el cambio dejaría la lista vacía (regla: al menos uno activo). */
    suspend fun toggle(providerId: String, enabled: Boolean): Boolean {
        var applied = true
        context.providerDataStore.edit { prefs ->
            val current = prefs[enabledKey]?.toMutableSet() ?: defaultIds().toMutableSet()
            if (enabled) current.add(providerId) else current.remove(providerId)
            if (current.isEmpty()) {
                applied = false
                return@edit
            }
            prefs[enabledKey] = current
        }
        return applied
    }

    suspend fun enableAll() {
        context.providerDataStore.edit { prefs ->
            prefs[enabledKey] = Providers.all.map { it.id }.toSet()
        }
    }
}
