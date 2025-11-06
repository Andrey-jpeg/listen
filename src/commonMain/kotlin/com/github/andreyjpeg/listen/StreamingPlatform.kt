package com.github.andreyjpeg.listen

enum class StreamingPlatform(val key: String, val displayName: String) {
    APPLE_MUSIC("appleMusic", "Apple Music"),
    SPOTIFY("spotify", "Spotify"),
    YOUTUBE_MUSIC("youtubeMusic", "YouTube Music"),
    YOUTUBE("youtube", "YouTube"),
    AMAZON_MUSIC("amazonMusic", "Amazon Music"),
    DEEZER("deezer", "Deezer"),
    TIDAL("tidal", "Tidal");

    override fun toString(): String = displayName.lowercase().replace(" ", "-")

    companion object {
        fun fromKey(input: String): StreamingPlatform {
            val candidate = input.trim()
            return entries.firstOrNull { it.matches(candidate) } ?: throw IllegalArgumentException(
                "Unsupported platform '$input'. Supported platforms: ${
                    entries.joinToString { it.toCliOption() }
                }"
            )
        }

        fun supportedOptions(): String = entries.joinToString(", ") { it.toCliOption() }
    }

    private fun matches(value: String): Boolean {
        val normalized = value.lowercase()
        val withoutSeparators = normalized.replace(" ", "").replace("-", "")
        return normalized == key.lowercase() ||
            withoutSeparators == key.lowercase().replace("-", "") ||
            normalized == name.lowercase() ||
            normalized == displayName.lowercase() ||
            normalized == toCliOption() ||
            withoutSeparators == displayName.lowercase().replace(" ", "") ||
            normalized == key.lowercase().replace(" ", "-")
    }

    fun toCliOption(): String = displayName.lowercase().replace(" ", "-")
}
