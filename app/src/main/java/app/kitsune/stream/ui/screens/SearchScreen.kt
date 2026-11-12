package app.kitsune.stream.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAnimeClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                TextField(
                    value = query,
                    onValueChange = { viewModel.query.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            })
        }
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val s = state) {
            SearchUi.Idle -> EmptyState(stringResource(R.string.search_idle_hint), modifier)
            SearchUi.Loading -> LoadingState(modifier)
            is SearchUi.Failure -> ErrorState(s.message, viewModel::retry, modifier)
            is SearchUi.Ready -> {
                if (s.items.isEmpty()) {
                    EmptyState(stringResource(R.string.empty_results), modifier)
                } else {
                    LazyColumn(
                        modifier = modifier,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(s.items, key = { it.anilistId }) { anime ->
                            AnimeRow(anime) { onAnimeClick(anime.anilistId) }
                        }
                    }
                }
            }
        }
    }
}

sealed interface SearchUi {
    data object Idle : SearchUi
    data object Loading : SearchUi
    data class Ready(val items: List<Anime>) : SearchUi
    data class Failure(val message: String) : SearchUi
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val catalog = (app as KitsuneApplication).container.catalog

    val query = MutableStateFlow("")
    private val retryTrigger = MutableStateFlow(0)

    val state: StateFlow<SearchUi> =
        combine(query.debounce(400), retryTrigger) { q, _ -> q.trim() }
            .transformLatest { q ->
                if (q.length < 3) {
                    emit(SearchUi.Idle)
                    return@transformLatest
                }
                emit(SearchUi.Loading)
                emit(
                    when (val r = catalog.search(q)) {
                        is CatalogResult.Ok -> SearchUi.Ready(r.data)
                        is CatalogResult.Error -> SearchUi.Failure(r.message)
                    }
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUi.Idle)

    fun retry() { retryTrigger.value++ }
}
