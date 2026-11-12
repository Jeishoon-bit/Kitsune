package app.kitsune.stream.data.catalog

import app.kitsune.stream.data.model.Anime

sealed interface CatalogResult<out T> {
    data class Ok<T>(val data: T) : CatalogResult<T>
    data class Error(val message: String) : CatalogResult<Nothing>
}

inline fun <T, R> CatalogResult<T>.map(transform: (T) -> R): CatalogResult<R> = when (this) {
    is CatalogResult.Ok -> CatalogResult.Ok(transform(data))
    is CatalogResult.Error -> this
}

enum class AniListSeason { WINTER, SPRING, SUMMER, FALL }

interface CatalogSource {
    suspend fun search(query: String, page: Int = 1): CatalogResult<List<Anime>>
    suspend fun detail(anilistId: Int): CatalogResult<Anime>
    suspend fun trending(page: Int = 1): CatalogResult<List<Anime>>
    suspend fun season(year: Int, season: AniListSeason): CatalogResult<List<Anime>>
}
