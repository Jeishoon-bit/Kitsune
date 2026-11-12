package app.kitsune.stream.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kitsune.stream.KitsuneApplication
import app.kitsune.stream.R
import app.kitsune.stream.data.catalog.CatalogResult
import app.kitsune.stream.data.model.Anime
import app.kitsune.stream.ui.components.ErrorState
import app.kitsune.stream.ui.components.LoadingState
import app.kitsune.stream.ui.components.UiState
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title = (state as? UiState.Success)?.data?.title ?: stringResource(R.string.loading)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val s = state) {
            UiState.Loading -> LoadingState(modifier)
            is UiState.Error -> ErrorState(s.message, viewModel::load, modifier)
            is UiState.Success -> {
                val a = s.data
                Column(
                    modifier.verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    AsyncImage(
                        model = a.coverImage,
                        contentDescription = a.title,
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(a.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        listOfNotNull(
                            a.seasonYear?.toString(),
                            a.episodes?.let { stringResource(R.string.episodes_format, it) },
                            a.averageScore?.let { stringResource(R.string.score_format, it) }
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        a.synopsis ?: stringResource(R.string.synopsis_missing),
                        style = MaterialTheme.typography.bodyLarge
                    )
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
