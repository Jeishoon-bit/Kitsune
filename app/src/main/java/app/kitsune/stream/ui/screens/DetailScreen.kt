// app/src/main/java/app/kitsune/stream/ui/screens/DetailScreen.kt
package app.kitsune.stream.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kitsune.stream.data.catalog.AniListCatalog
import app.kitsune.stream.data.model.Anime
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(malId: Int, onBack: () -> Unit) {
    val catalog = remember { AniListCatalog() }
    var anime by remember { mutableStateOf<Anime?>(null) }

    LaunchedEffect(malId) { anime = catalog.detail(malId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(anime?.title ?: "Cargando…") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            anime?.let { a ->
                AsyncImage(
                    model = a.coverImage,
                    contentDescription = a.title,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(a.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    listOfNotNull(
                        a.seasonYear?.toString(),
                        a.episodes?.let { "$it episodios" },
                        a.averageScore?.let { "$it%" }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(a.synopsis ?: "Sin sinopsis disponible.", style = MaterialTheme.typography.bodyLarge)
            } ?: CircularProgressIndicator()
        }
    }
}
