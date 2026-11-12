package app.kitsune.stream.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kitsune.stream.KitsuneApplication
import app.kitsune.stream.R
import app.kitsune.stream.data.catalog.CatalogResult
import app.kitsune.stream.data.model.Anime
import app.kitsune.stream.ui.components.AnimeRow
import app.kitsune.stream.ui.components.EmptyState
import app.kitsune.stream.ui.components.ErrorState
import app.kitsune.stream.ui.components.LoadingState
import app.kitsune.stream.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
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
                if (s.data.isEmpty()) {
                    EmptyState(stringResource(R.string.empty_results), modifier)
                } else {
                    LazyColumn(
                        modifier = modifier,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(s.data, key = { it.anilistId }) { anime ->
                            AnimeRow(anime) { onAnimeClick(anime.anilistId) }
                        }
                    }
                }
            }
        }
    }
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val catalog = (app as KitsuneApplication).container.catalog

    private val _state = MutableStateFlow<UiState<List<Anime>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Anime>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = when (val r = catalog.trending()) {
                is CatalogResult.Ok -> UiState.Success(r.data)
                is CatalogResult.Error -> UiState.Error(r.message)
            }
        }
    }
}
