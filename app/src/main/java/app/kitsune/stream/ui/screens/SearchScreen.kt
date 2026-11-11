// app/src/main/java/app/kitsune/stream/ui/screens/SearchScreen.kt
package app.kitsune.stream.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kitsune.stream.data.catalog.AniListCatalog
import app.kitsune.stream.data.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onAnimeClick: (Int) -> Unit) {
    val catalog = remember { AniListCatalog() }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Anime>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.length >= 3) {
            kotlinx.coroutines.delay(400)
            results = catalog.search(query)
        } else results = emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar anime…") },
                    singleLine = true
                )
            })
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(results, key = { it.anilistId }) { anime ->
                AnimeRow(anime) { anime.malId?.let(onAnimeClick) }
            }
        }
    }
}
