package app.kitsune.stream.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kitsune.stream.KitsuneApplication
import app.kitsune.stream.R
import app.kitsune.stream.data.catalog.CatalogResult
import app.kitsune.stream.data.model.Anime
import app.kitsune.stream.ui.components.ErrorState
import app.kitsune.stream.ui.components.GenreChips
import app.kitsune.stream.ui.components.LoadingState
import app.kitsune.stream.ui.components.MetaRow
import app.kitsune.stream.ui.components.PosterRow
import app.kitsune.stream.ui.components.SectionHeader
import app.kitsune.stream.ui.components.UiState
import app.kitsune.stream.ui.components.countdownLabel
import coil.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val hero by viewModel.hero.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()

    when (val s = sections) {
        UiState.Loading -> LoadingState(Modifier.fillMaxSize())
        is UiState.Error -> ErrorState(s.message, viewModel::loadSections, Modifier.fillMaxSize())
        is UiState.Success -> {
            val genres = s.data.trending.flatMap { it.genres }.distinct().take(7)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                Box {
                    when (val h = hero) {
                        UiState.Loading -> LoadingState(Modifier.fillMaxWidth().height(520.dp))
                        is UiState.Error -> ErrorState(h.message, viewModel::reloadHero, Modifier.fillMaxWidth().height(520.dp))
                        is UiState.Success -> HeroCarousel(h.data, onAnimeClick)
                    }
                    Column(Modifier.align(Alignment.TopStart).padding(top = 24.dp, start = 16.dp, end = 16.dp)) {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(12.dp))
                        GenreChips(genres, genre, viewModel::selectGenre)
                    }
                }
                Spacer(Modifier.height(20.dp))
                SectionHeader(stringResource(R.string.section_new_episodes))
                PosterRow(s.data.airing, onAnimeClick)
                Spacer(Modifier.height(20.dp))
                SectionHeader(stringResource(R.string.section_trending))
                PosterRow(s.data.trending, onAnimeClick)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeroCarousel(items: List<Anime>, onAnimeClick: (Int) -> Unit) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    Box(Modifier.fillMaxWidth().height(520.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val anime = items[page]
            Box(Modifier.fillMaxSize().clickable { onAnimeClick(anime.anilistId) }) {
                AsyncImage(
                    model = anime.bannerImage ?: anime.coverExtraLarge ?: anime.coverImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                )
                val countdown = countdownLabel(anime.nextAiringAt)
                if (countdown != null && anime.nextEpisode != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.45f)
                    ) {
                        Text(
                            "EP ${anime.nextEpisode}  $countdown",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        "${page + 1} / ${items.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MetaRow(anime)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        anime.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        anime.genres.take(3).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(items.size) { i ->
                val active = i == pagerState.currentPage
                Box(
                    Modifier
                        .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                )
            }
        }
    }
}

data class HomeSections(val airing: List<Anime>, val trending: List<Anime>)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val catalog = (app as KitsuneApplication).container.catalog

    private val _sections = MutableStateFlow<UiState<HomeSections>>(UiState.Loading)
    val sections: StateFlow<UiState<HomeSections>> = _sections.asStateFlow()

    private val _hero = MutableStateFlow<UiState<List<Anime>>>(UiState.Loading)
    val hero: StateFlow<UiState<List<Anime>>> = _hero.asStateFlow()

    private val _genre = MutableStateFlow<String?>(null)
    val genre: StateFlow<String?> = _genre.asStateFlow()

    init {
        loadSections()
        reloadHero()
    }

    fun loadSections() {
        viewModelScope.launch {
            _sections.value = UiState.Loading
            _sections.value = try {
                coroutineScope {
                    val airing = async { catalog.airing() }
                    val trending = async { catalog.trending() }
                    val a = airing.await()
                    val t = trending.await()
                    when {
                        a is CatalogResult.Ok && t is CatalogResult.Ok ->
                            UiState.Success(HomeSections(a.data, t.data))
                        a is CatalogResult.Error -> UiState.Error(a.message)
                        t is CatalogResult.Error -> UiState.Error(t.message)
                        else -> UiState.Error("Error desconocido")
                    }
                }
            } catch (e: Exception) {
                UiState.Error("No se pudo cargar el inicio.")
            }
        }
    }

    fun selectGenre(genre: String?) {
        _genre.value = genre
        reloadHero()
    }

    fun reloadHero() {
        viewModelScope.launch {
            _hero.value = UiState.Loading
            val g = _genre.value
            val r = if (g == null) catalog.trending() else catalog.byGenre(g)
            _hero.value = when (r) {
                is CatalogResult.Ok -> UiState.Success(r.data.filter { it.bannerImage != null }.take(6).ifEmpty { r.data.take(6) })
                is CatalogResult.Error -> UiState.Error(r.message)
            }
        }
    }
}
