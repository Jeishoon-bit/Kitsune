package app.kitsune.stream.data.catalog

import app.kitsune.stream.data.model.Anime
import app.kitsune.stream.data.model.RelatedAnime
import app.kitsune.stream.data.model.StreamEpisode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AniListCatalog(private val client: OkHttpClient) : CatalogSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private class RateLimitException(val retryAfterSeconds: Long?) : IOException()

    private val listFields = """
        id
        idMal
        title { romaji english }
        coverImage { extraLarge large }
        bannerImage
        episodes
        status
        averageScore
        popularity
        genres
        seasonYear
        format
        duration
        trailer { id site }
        nextAiringEpisode { episode airingAt }
    """.trimIndent()

    private val detailFields = """
        $listFields
        description(asHtml: false)
        studios(isMain: true) { nodes { name } }
        streamingEpisodes { title thumbnail url site }
        relations {
          edges {
            relationType(version: 2)
            node {
              id
              title { romaji }
              coverImage { large }
              seasonYear
              format
            }
          }
        }
    """.trimIndent()

    private suspend fun post(
        query: String,
        variables: JsonObject,
        allowRetry: Boolean = true
    ): CatalogResult<JsonObject> = try {
        val payload = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }.toString()

        val request = Request.Builder()
            .url(ENDPOINT)
            .post(payload.toRequestBody("application/json".toMediaType()))
            .header("Accept", "application/json")
            .build()

        val root = runInterruptible(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (response.code == 429) {
                    throw RateLimitException(response.header("Retry-After")?.toLongOrNull())
                }
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body?.string() ?: throw IOException("Respuesta vacía")
                json.parseToJsonElement(body).jsonObject
            }
        }

        val gqlError = root["errors"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        if (gqlError != null) CatalogResult.Error(gqlError) else CatalogResult.Ok(root)
    } catch (e: CancellationException) {
        throw e
    } catch (e: RateLimitException) {
        if (allowRetry) {
            delay((e.retryAfterSeconds ?: 30L).coerceIn(1L, 10L) * 1000L)
            post(query, variables, allowRetry = false)
        } else {
            CatalogResult.Error("AniList está limitando las peticiones. Inténtalo de nuevo en un minuto.")
        }
    } catch (e: IOException) {
        CatalogResult.Error("No se pudo conectar con AniList. Revisa tu conexión.")
    } catch (e: SerializationException) {
        CatalogResult.Error("AniList devolvió una respuesta inválida.")
    } catch (e: IllegalArgumentException) {
        CatalogResult.Error("AniList devolvió una respuesta inválida.")
    }

    private fun parseMedia(obj: JsonObject): Anime? {
        val id = obj["id"]?.jsonPrimitive?.intOrNull ?: return null
        val titleObj = obj["title"]?.jsonObject
        val cover = obj["coverImage"]?.jsonObject
        val trailer = obj["trailer"]?.jsonObject
        val trailerUrl = trailer?.get("id")?.jsonPrimitive?.contentOrNull?.let { tid ->
            when (trailer["site"]?.jsonPrimitive?.contentOrNull) {
                "youtube" -> "https://www.youtube.com/watch?v=$tid"
                "dailymotion" -> "https://www.dailymotion.com/video/$tid"
                else -> null
            }
        }
        val next = obj["nextAiringEpisode"]?.jsonObject
        val episodes = obj["streamingEpisodes"]?.jsonArray?.mapNotNull { el ->
            val e = el.jsonObject
            val url = e["url"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val rawTitle = e["title"]?.jsonPrimitive?.contentOrNull ?: "Episodio"
            StreamEpisode(
                number = Regex("""Episode\s+(\d+)""", RegexOption.IGNORE_CASE)
                    .find(rawTitle)?.groupValues?.get(1)?.toIntOrNull(),
                title = rawTitle,
                thumbnail = e["thumbnail"]?.jsonPrimitive?.contentOrNull,
                url = url,
                site = e["site"]?.jsonPrimitive?.contentOrNull ?: "Oficial"
            )
        } ?: emptyList()
        val relations = obj["relations"]?.jsonObject?.get("edges")?.jsonArray?.mapNotNull { el ->
            val edge = el.jsonObject
            val node = edge["node"]?.jsonObject ?: return@mapNotNull null
            val rid = node["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            RelatedAnime(
                anilistId = rid,
                relation = edge["relationType"]?.jsonPrimitive?.contentOrNull ?: "RELATED",
                title = node["title"]?.jsonObject?.get("romaji")?.jsonPrimitive?.contentOrNull ?: "Sin título",
                coverImage = node["coverImage"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull,
                seasonYear = node["seasonYear"]?.jsonPrimitive?.intOrNull,
                format = node["format"]?.jsonPrimitive?.contentOrNull
            )
        } ?: emptyList()

        return Anime(
            anilistId = id,
            malId = obj["idMal"]?.jsonPrimitive?.intOrNull,
            title = titleObj?.get("romaji")?.jsonPrimitive?.contentOrNull ?: "Sin título",
            titleEnglish = titleObj?.get("english")?.jsonPrimitive?.contentOrNull,
            synopsis = obj["description"]?.jsonPrimitive?.contentOrNull
                ?.replace(Regex("<[^>]*>"), "")?.trim(),
            coverImage = cover?.get("large")?.jsonPrimitive?.contentOrNull,
            coverExtraLarge = cover?.get("extraLarge")?.jsonPrimitive?.contentOrNull,
            bannerImage = obj["bannerImage"]?.jsonPrimitive?.contentOrNull,
            episodes = obj["episodes"]?.jsonPrimitive?.intOrNull,
            status = obj["status"]?.jsonPrimitive?.contentOrNull,
            averageScore = obj["averageScore"]?.jsonPrimitive?.intOrNull,
            popularity = obj["popularity"]?.jsonPrimitive?.intOrNull,
            genres = obj["genres"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
            seasonYear = obj["seasonYear"]?.jsonPrimitive?.intOrNull,
            format = obj["format"]?.jsonPrimitive?.contentOrNull,
            duration = obj["duration"]?.jsonPrimitive?.intOrNull,
            studio = obj["studios"]?.jsonObject?.get("nodes")?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull,
            trailerUrl = trailerUrl,
            nextEpisode = next?.get("episode")?.jsonPrimitive?.intOrNull,
            nextAiringAt = next?.get("airingAt")?.jsonPrimitive?.longOrNull,
            streamingEpisodes = episodes,
            relations = relations
        )
    }

    private fun parsePage(root: JsonObject): List<Anime> {
        val media = root["data"]?.jsonObject?.get("Page")?.jsonObject?.get("media")?.jsonArray
            ?: return emptyList()
        return media.mapNotNull { parseMedia(it.jsonObject) }
    }

    private suspend fun pageQuery(
        extraArgs: String,
        variables: JsonObject,
        sort: String,
        perPage: Int = 20
    ): CatalogResult<List<Anime>> {
        val q = """
            query (${'$'}page: Int${'$'}{extraArgs}) {
              Page(page: ${'$'}page, perPage: $perPage) {
                media(type: ANIME, isAdult: false, sort: $sort${'$'}{if (extraArgs.contains("genre")) ", genre: ${'$'}genre" else ""}) {
                  $listFields
                }
              }
            }
        """.trimIndent()
        return post(q, variables).map { parsePage(it) }
    }

    override suspend fun search(query: String, page: Int): CatalogResult<List<Anime>> {
        val q = """
            query (${'$'}search: String, ${'$'}page: Int) {
              Page(page: ${'$'}page, perPage: 20) {
                media(search: ${'$'}search, type: ANIME, isAdult: false, sort: SEARCH_MATCH) {
                  $listFields
                }
              }
            }
        """.trimIndent()
        return post(q, buildJsonObject {
            put("search", query)
            put("page", page)
        }).map { parsePage(it) }
    }

    override suspend fun detail(anilistId: Int): CatalogResult<Anime> {
        val q = """
            query (${'$'}id: Int) {
              Media(id: ${'$'}id, type: ANIME) {
                $detailFields
              }
            }
        """.trimIndent()
        return when (val r = post(q, buildJsonObject { put("id", anilistId) })) {
            is CatalogResult.Error -> r
            is CatalogResult.Ok -> {
                val anime = r.data["data"]?.jsonObject?.get("Media")?.jsonObject?.let(::parseMedia)
                if (anime != null) CatalogResult.Ok(anime)
                else CatalogResult.Error("No se encontró el anime.")
            }
        }
    }

    override suspend fun trending(page: Int): CatalogResult<List<Anime>> {
        val q = """
            query (${'$'}page: Int) {
              Page(page: ${'$'}page, perPage: 20) {
                media(type: ANIME, sort: TRENDING_DESC, isAdult: false) {
                  $listFields
                }
              }
            }
        """.trimIndent()
        return post(q, buildJsonObject { put("page", page) }).map { parsePage(it) }
    }

    override suspend fun airing(page: Int): CatalogResult<List<Anime>> {
        val q = """
            query (${'$'}page: Int) {
              Page(page: ${'$'}page, perPage: 20) {
                media(type: ANIME, status: RELEASING, sort: TRENDING_DESC, isAdult: false) {
                  $listFields
                }
              }
            }
        """.trimIndent()
        return post(q, buildJsonObject { put("page", page) }).map { parsePage(it) }
    }

    override suspend fun byGenre(genre: String, page: Int): CatalogResult<List<Anime>> {
        val q = """
            query (${'$'}page: Int, ${'$'}genre: String) {
              Page(page: ${'$'}page, perPage: 20) {
                media(type: ANIME, genre: ${'$'}genre, sort: POPULARITY_DESC, isAdult: false) {
                  $listFields
                }
              }
            }
        """.trimIndent()
        return post(q, buildJsonObject {
            put("page", page)
            put("genre", genre)
        }).map { parsePage(it) }
    }

    override suspend fun season(year: Int, season: AniListSeason): CatalogResult<List<Anime>> {
        val q = """
            query (${'$'}year: Int, ${'$'}season: MediaSeason) {
              Page(page: 1, perPage: 30) {
                media(type: ANIME, seasonYear: ${'$'}year, season: ${'$'}season, sort: POPULARITY_DESC) {
                  $listFields
                }
              }
            }
        """.trimIndent()
        return post(q, buildJsonObject {
            put("year", year)
            put("season", season.name)
        }).map { parsePage(it) }
    }

    companion object {
        private const val ENDPOINT = "https://graphql.anilist.co"
    }
}
