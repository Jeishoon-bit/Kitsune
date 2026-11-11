package app.kitsune.stream.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val enabled by store.enabledIds.collectAsState(initial = emptySet())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enabled servers", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) { Text("✕") }
            }
            Text(
                "${enabled.size} of ${Providers.all.size} on",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "A server that is off is never asked for episodes and never offered in the player. At least one has to stay on.",
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
                            scope.launch { store.toggle(provider.id, checked) }
                        }
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { scope.launch { store.enableAll() } }) {
                    Text("Turn all on")
                }
                TextButton(onClick = onDismiss) { Text("Done") }
            }
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
                "Fast",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
        }
        Checkbox(checked = checked, onCheckedChange = onToggle)
    }
}
