package app.kitsune.stream.data.provider

enum class ProviderLanguage { ES, EN, MULTI }

data class Provider(
    val id: String,
    val name: String,
    val language: ProviderLanguage,
    val isFast: Boolean = false,
    val isCore: Boolean = false,
    val baseUrl: String
)

object Providers {
    val Anikoto = Provider(
        id = "anikoto",
        name = "Anikoto",
        language = ProviderLanguage.EN,
        isFast = true,
        isCore = true,
        baseUrl = "https://anikoto.cz"
    )

    val AnimeAV1 = Provider(
        id = "animeav1",
        name = "AnimeAV1",
        language = ProviderLanguage.ES,
        isCore = true,
        baseUrl = "https://animeav1.com"
    )

    val all = listOf(Anikoto, AnimeAV1)

    fun byId(id: String): Provider? = all.firstOrNull { it.id == id }
}
