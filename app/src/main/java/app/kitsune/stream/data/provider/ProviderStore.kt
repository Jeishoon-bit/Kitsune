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

    val enabledIds: Flow<Set<String>> = context.providerDataStore.data.map { prefs ->
        prefs[enabledKey] ?: Providers.all.filter { it.isCore }.map { it.id }.toSet()
    }

    suspend fun toggle(providerId: String, enabled: Boolean) {
        context.providerDataStore.edit { prefs ->
            val current = prefs[enabledKey]?.toMutableSet()
                ?: Providers.all.filter { it.isCore }.map { it.id }.toMutableSet()

            if (enabled) current.add(providerId) else current.remove(providerId)

            // Regla: al menos uno activo
            if (current.isEmpty()) return@edit

            prefs[enabledKey] = current
        }
    }

    suspend fun enableAll() {
        context.providerDataStore.edit { prefs ->
            prefs[enabledKey] = Providers.all.map { it.id }.toSet()
        }
    }
}
