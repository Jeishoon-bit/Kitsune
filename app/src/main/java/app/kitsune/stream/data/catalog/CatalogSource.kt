package app.kitsune.stream.data.catalog

import app.kitsune.stream.data.model.Anime

interface CatalogSource {
    suspend fun search(query: String, page: Int = 1): List<Anime>
    suspend fun detail(malId: Int): Anime?
    suspend fun trending(page: Int = 1): List<Anime>
    suspend fun season(year: Int, season: String): List<Anime>
}
