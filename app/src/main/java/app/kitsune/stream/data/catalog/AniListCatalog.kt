package app.kitsune.stream.data.catalog

import app.kitsune.stream.data.model.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AniListCatalog(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) : CatalogSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val endpoint = "https://graphql.anilist.co"

    private val mediaFields = """
        id
        idMal
        title { romaji english }
        description(asHtml: false)
        coverImage { large }
        episodes
        status
        averageScore
        genres
        seasonYear
    """.trimIndent()

    private suspend fun post(query: String, variables: JsonObject): JsonObject? =
        withContext(Dispatchers.IO) {
            val payload = buildJsonObject {
                put("query", query)
                put("variables", variables)
            }.toString()

            val request = Request.Builder()
                .url(endpoint)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .build()

            runCatching {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    json.parseToJsonElement(response.body?.string() ?: return@use null).jsonObject
                }
            }.getOrNull()
        }

    private fun parseMedia(obj: JsonObject): Anime? {
        val id = obj["id"]?.jsonPrimitive?.intOrNull ?: return null
        val titleObj = obj["title"]?.jsonObject
        return Anime(
            anilistId = id,
            malId = obj["idMal"]?.jsonPrimitive?.intOrNull,
            title = titleObj?.get("romaji")?.jsonPrimitive?.contentOrNull ?: "Sin título",
            titleEnglish = titleObj?.get("english")?.jsonPrimitive?.contentOrNull,
            synopsis = obj["description"]?.jsonPrimitive?.contentOrNull
                ?.replace(Regex("<[^>]*>"), "")
                ?.trim(),
            coverImage = obj["coverImage"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull,
            episodes = obj["episodes"]?.jsonPrimitive?.intOrNull,
            status = obj["status"]?.jsonPrimitive?.contentOrNull,
            averageScore = obj["averageScore"]?.jsonPrimitive?.intOrNull,
            genres = obj["genres"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
            seasonYear = obj["seasonYear"]?.jsonPrimitive?.intOrNull
        )
    }

    private fun parsePage(root: JsonObject?): List<Anime> {
        val media = root?.get("data")?.jsonObject
            ?.get("Page")?.jsonObject
            ?.get("media")?.jsonArray ?: return emptyList()
        return media.mapNotNull { parseMedia(it.jsonObject) }
    }

    override suspend fun search(query: String, page: Int): List<Anime> {
        val q = """
            query (${'$'}search: String, ${'$'}page: Int) {
              Page(page: ${'$'}page, perPage: 20) {
                media(search: ${'$'}search, type: ANIME, isAdult: false, sort: SEARCH_MATCH) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()
        return parsePage(post(q, buildJsonObject {
            put("search", query)
            put("page", page)
        }))
    }

    override suspend fun detail(malId: Int): Anime? {
        val q = """
            query (${'$'}idMal: Int) {
              Media(idMal: ${'$'}idMal, type: ANIME) {
                $mediaFields
              }
            }
        """.trimIndent()
        val root = post(q, buildJsonObject { put("idMal", malId) })
        val media = root?.get("data")?.jsonObject?.get("Media")?.jsonObject ?: return null
        return parseMedia(media)
    }

    override suspend fun trending(page: Int): List<Anime> {
        val q = """
            query (${'$'}page: Int) {
              Page(page: ${'$'}page, perPage: 20) {
                media(type: ANIME, sort: TRENDING_DESC, isAdult: false) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()
        return parsePage(post(q, buildJsonObject { put("page", page) }))
    }

    override suspend fun season(year: Int, season: String): List<Anime> {
        val q = """
            query (${'$'}year: Int, ${'$'}season: MediaSeason) {
              Page(page: 1, perPage: 30) {
                media(type: ANIME, seasonYear: ${'$'}year, season: ${'$'}season, sort: POPULARITY_DESC) {
                  $mediaFields
                }
              }
            }
        """.trimIndent()
        return parsePage(post(q, buildJsonObject {
            put("year", year)
            put("season", season)
        }))
    }
}
