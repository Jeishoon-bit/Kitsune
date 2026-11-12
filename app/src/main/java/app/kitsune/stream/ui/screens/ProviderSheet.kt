package app.kitsune.stream.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kitsune.stream.R
import app.kitsune.stream.data.provider.Provider
import app.kitsune.stream.data.provider.ProviderStore
import app.kitsune.stream.data.provider.Providers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSheet(
    store: ProviderStore,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val enabled by store.enabledIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val minOneMessage = stringResource(R.string.providers_min_one)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.providers_title), style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                }
            }
            Text(
                stringResource(R.string.providers_enabled_count, enabled.size, Providers.all.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.providers_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(Providers.all, key = { it.id }) { provider ->
                    ProviderRow(
                        provider = provider,
                        checked = provider.id in enabled,
                        onToggle = { checked ->
                            scope.launch {
                                val applied = store.toggle(provider.id, checked)
                                if (!applied) snackbarHostState.showSnackbar(minOneMessage)
                            }
                        }
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { scope.launch { store.enableAll() } }) {
                    Text(stringResource(R.string.action_enable_all))
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_done)) }
            }
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun ProviderRow(
    provider: Provider,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(provider.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (provider.isFast) {
            Text(
                stringResource(R.string.provider_fast),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
