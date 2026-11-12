package app.kitsune.stream.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    val anilistId: Int,
    val malId: Int? = null,
    val title: String,
    val titleEnglish: String? = null,
    val synopsis: String? = null,
    val coverImage: String? = null,
    val coverExtraLarge: String? = null,
    val bannerImage: String? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val averageScore: Int? = null,
    val popularity: Int? = null,
    val genres: List<String> = emptyList(),
    val seasonYear: Int? = null,
    val format: String? = null,
    val duration: Int? = null,
    val studio: String? = null,
    val trailerUrl: String? = null,
    val nextEpisode: Int? = null,
    val nextAiringAt: Long? = null,
    val streamingEpisodes: List<StreamEpisode> = emptyList(),
    val relations: List<RelatedAnime> = emptyList()
)

@Serializable
data class StreamEpisode(
    val number: Int?,
    val title: String,
    val thumbnail: String?,
    val url: String,
    val site: String
)

@Serializable
data class RelatedAnime(
    val anilistId: Int,
    val relation: String,
    val title: String,
    val coverImage: String?,
    val seasonYear: Int?,
    val format: String?
)
