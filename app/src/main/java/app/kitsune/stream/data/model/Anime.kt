package app.kitsune.stream.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    val anilistId: Int,
    val malId: Int?,
    val title: String,
    val titleEnglish: String? = null,
    val synopsis: String? = null,
    val coverImage: String? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val averageScore: Int? = null,
    val genres: List<String> = emptyList(),
    val seasonYear: Int? = null
)
