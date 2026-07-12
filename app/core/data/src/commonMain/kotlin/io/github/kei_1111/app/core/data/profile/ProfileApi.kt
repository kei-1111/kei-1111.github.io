package io.github.kei_1111.app.core.data.profile

import io.github.kei_1111.shared.model.GitHubProfile
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

internal fun parseProfile(body: String): GitHubProfile? = try {
    json.decodeFromString<GitHubProfile>(body)
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}
