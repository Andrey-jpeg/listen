package com.github.andreyjpeg.listen

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SongLinkResolver(
    private val client: HttpClient = createHttpClient()
) {
    suspend fun resolveAll(sourceUrl: String): List<SongLinkResult> {
        val response = fetchSongLinkResponse(sourceUrl)

        return StreamingPlatform.entries.mapNotNull { platform ->
            response.linksByPlatform[platform.key]?.let { link ->
                SongLinkResult(
                    convertedUrl = link.url,
                    sourcePageUrl = response.pageUrl,
                    targetPlatform = platform
                )
            }
        }
    }

    fun close() {
        client.close()
    }

    private suspend fun fetchSongLinkResponse(sourceUrl: String): SongLinkResponse {
        val countryCode = currentCountryCode()
            ?.takeIf { it.length == 2 && it.all { ch -> ch in 'A'..'Z' } }
        return runCatching {
            client.get("https://api.song.link/v1-alpha.1/links") {
                parameter("url", sourceUrl)
                countryCode?.let { parameter("userCountry", it) }
            }.body<SongLinkResponse>()
        }.getOrElse { throwable ->
            throw SongLinkResolverException(
                "Failed to query song.link: ${throwable.message ?: "unknown error"}",
                throwable
            )
        }
    }
}

data class SongLinkResult(
    val convertedUrl: String,
    val sourcePageUrl: String,
    val targetPlatform: StreamingPlatform
)

class SongLinkResolverException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

@Serializable
private data class SongLinkResponse(
    val pageUrl: String,
    val linksByPlatform: Map<String, SongLinkPlatformLink> = emptyMap()
)

@Serializable
private data class SongLinkPlatformLink(
    val url: String,
    @SerialName("entityUniqueId") val entityUniqueId: String? = null
)
