package app.kitsune.stream.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kitsune.stream.KitsuneApplication
import app.kitsune.stream.R
import app.kitsune.stream.data.catalog.CatalogResult
import app.kitsune.stream.data.model.Anime
import app.kitsune.stream.data.model.RelatedAnime
import app.kitsune.stream.ui.components.ErrorState
import app.kitsune.stream.ui.components.LoadingState
import app.kitsune.stream.ui.components.PosterCard
import app.kitsune.stream.ui.components.UiState
import app.kitsune.stream.ui.components.formatCompact
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
    }

    when (val s = state) {
        UiState.Loading -> LoadingState(Modifier.fillMaxSize())
        is UiState.Error -> ErrorState(s.message, viewModel::load, Modifier.fillMaxSize())
        is UiState.Success -> {
            val a = s.data
            var tab by remember { mutableIntStateOf(0) }
            val seasons = remember(a.anilistId) {
                (listOf(
                    RelatedAnime(a.anilistId, "CURRENT", a.title, a.coverImage, a.seasonYear, a.format)
                ) + a.relations.filter { it.relation == "SEQUEL" || it.relation == "PREQUEL" })
                    .sortedBy { it.seasonYear ?: 9999 }
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Box(Modifier.fillMaxWidth().height(280.dp)) {
                        AsyncImage(
                            model = a.bannerImage ?: a.coverExtraLarge ?: a.coverImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent, MaterialTheme.colorScheme.background))
                            )
                        )
                        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(8.dp)) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back), tint = Color.White)
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "https://anilist.co/anime/${a.anilistId}")
                                }
                                context.startActivity(Intent.createChooser(send, null))
                            }) {
                                Icon(Icons.Filled.Share, stringResource(R.string.action_share), tint = Color.White)
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.padding(start = 16.dp, end = 16.dp).padding(top = 0.dp)) {
                        AsyncImage(
                            model = a.coverExtraLarge ?: a.coverImage,
                            contentDescription = a.title,
                            modifier = Modifier.width(110.dp).height(160.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(8.dp))
                            a.studio?.let {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(it, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            val stats = listOfNotNull(
                                a.averageScore?.let { stringResource(R.string.score_format, it) },
                                a.popularity?.let { stringResource(R.string.members_format, formatCompact(it)) },
                                a.episodes?.let { stringResource(R.string.episodes_format, it) }
                            ).joinToString(" · ")
                            Text(stats, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                val url = a.streamingEpisodes.firstOrNull()?.url ?: a.trailerUrl ?: "https://anilist.co/anime/${a.anilistId}"
                                open(url)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_watch))
                        }
                        if (a.trailerUrl != null) {
                            OutlinedButton(onClick = { open(a.trailerUrl!!) }) {
                                Text(stringResource(R.string.action_trailer))
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                        listOf(R.string.tab_overview, R.string.tab_episodes, R.string.tab_related).forEachIndexed { i, res ->
                            val selected = tab == i
                            Surface(
                                shape = CircleShape,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.clickable { tab = i }
                            ) {
                                Text(
                                    stringResource(res),
                                    Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if (seasons.size > 1) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            seasons.forEachIndexed { i, rel ->
                                val selected = rel.anilistId == a.anilistId
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { if (!selected) onAnimeClick(rel.anilistId) }
                                ) {
                                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(R.string.season_label, i + 1), style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                        rel.seasonYear?.let {
                                            Text(it.toString(), style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
                when (tab) {
                    0 -> item {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text(a.synopsis ?: stringResource(R.string.synopsis_missing), style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(16.dp))
                            Text(a.genres.joinToString("  ·  "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    1 -> {
                        if (a.streamingEpisodes.isEmpty()) {
                            item {
                                Column(Modifier.padding(16.dp)) {
                                    Text(stringResource(R.string.episodes_official_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = { open("https://anilist.co/anime/${a.anilistId}") }) {
                                        Text(stringResource(R.string.action_open_anilist))
                                    }
                                }
                            }
                        } else {
                            items(a.streamingEpisodes, key = { it.url }) { ep ->
                                Row(
                                    Modifier.fillMaxWidth().clickable { open(ep.url) }.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ep.thumbnail,
                                        contentDescription = null,
                                        modifier = Modifier.width(120.dp).height(68.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            ep.number?.let { stringResource(R.string.episode_label, it) } ?: ep.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(ep.site, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("↗", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    else -> {
                        val related = a.relations.filter { it.relation != "CURRENT" }
                        if (related.isEmpty()) {
                            item { Text(stringResource(R.string.related_empty), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        } else {
                            items(related.chunked(3)) { rowItems ->
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    rowItems.forEach { rel ->
                                        PosterCard(
                                            anime = Anime(anilistId = rel.anilistId, title = rel.title, coverImage = rel.coverImage, seasonYear = rel.seasonYear),
                                            onClick = { onAnimeClick(rel.anilistId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class DetailViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val catalog = (app as KitsuneApplication).container.catalog
    private val anilistId: Int = checkNotNull(savedStateHandle.get<Int>("anilistId"))

    private val _state = MutableStateFlow<UiState<Anime>>(UiState.Loading)
    val state: StateFlow<UiState<Anime>> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = when (val r = catalog.detail(anilistId)) {
                is CatalogResult.Ok -> UiState.Success(r.data)
                is CatalogResult.Error -> UiState.Error(r.message)
            }
        }
    }
}
