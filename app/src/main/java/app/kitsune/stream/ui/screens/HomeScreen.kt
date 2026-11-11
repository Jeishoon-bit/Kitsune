package app.kitsune.stream.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kitsune.stream.data.catalog.AniListCatalog
import app.kitsune.stream.data.model.Anime
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val catalog = remember { AniListCatalog() }
    var items by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        items = catalog.trending()
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitsune") },
                actions = {
                    TextButton(onClick = onSearchClick) { Text("Buscar") }
                    TextButton(onClick = onSettingsClick) { Text("Ajustes") }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.anilistId }) { anime ->
                    AnimeRow(anime) { anime.malId?.let(onAnimeClick) }
                }
            }
        }
    }
}

@Composable
fun AnimeRow(anime: Anime, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.title,
                modifier = Modifier.size(56.dp, 78.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    listOfNotNull(
                        anime.seasonYear?.toString(),
                        anime.episodes?.let { "$it eps" },
                        anime.averageScore?.let { "$it%" }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
