package io.github.kei_1111.test.conventions

import org.junit.jupiter.api.Test

class ServerConventionsTest {

    @Test
    fun serverClientDtosNeverAnnotatePropertiesWithSerialName() {
        val violations = serverScope.files
            .filter { "/client/" in it.path.normalizedPath }
            .flatMap { it.properties(includeNested = true) }
            .filter { !it.isTopLevel && it.hasAnnotationWithName("SerialName") }
            .map { it.location }

        assertNoViolations(violations, SERVER_RULE, GITHUB_PROFILE_SOURCE_REFERENCE)
    }

    @Test
    fun routeFilesDeclareExactlyOneInternalRouteExtension() {
        val violations = serverScope.files
            .filter { ROUTES_FILE_REGEX.containsMatchIn(it.path.normalizedPath) }
            .mapNotNull { file ->
                val route = file.functions(includeNested = false, includeLocal = false).singleOrNull()
                val valid = route != null &&
                    route.hasInternalModifier &&
                    route.receiverType?.name?.substringBefore('<') == "Route"
                file.path.takeUnless { valid }
            }

        assertNoViolations(violations, SERVER_RULE, PROFILE_ROUTES_REFERENCE)
    }

    @Test
    fun pluginFilesDeclareExactlyOneConfigureApplicationExtension() {
        val violations = serverScope.files
            .filter { "/plugins/" in it.path.normalizedPath }
            .mapNotNull { file ->
                val configure = file.functions(includeNested = false, includeLocal = false).singleOrNull()
                val valid = configure != null &&
                    CONFIGURE_FUNCTION_REGEX.containsMatchIn(configure.name) &&
                    configure.receiverType?.name?.substringBefore('<') == "Application"
                file.path.takeUnless { valid }
            }

        assertNoViolations(violations, SERVER_RULE, SERIALIZATION_PLUGIN_REFERENCE)
    }

    @Test
    fun ttlCachesAlwaysPassAnExplicitName() {
        val violations = serverScope.files
            .filter { "/service/" in it.path.normalizedPath }
            .mapNotNull { file ->
                val constructorCalls = TTL_CACHE_CALL_REGEX.findAll(file.text).count()
                val namedConstructorCalls = NAMED_TTL_CACHE_CALL_REGEX.findAll(file.text).count()
                file.path.takeUnless { constructorCalls == namedConstructorCalls }
            }

        assertNoViolations(violations, SERVER_RULE, GITHUB_PROFILE_SOURCE_REFERENCE)
    }

    @Test
    fun gitHubSourcesHoldOneQueryAndOneFetch() {
        val violations = serverScope.files
            .filter { GITHUB_SOURCE_FILE_REGEX.containsMatchIn(it.path.normalizedPath) }
            .mapNotNull { file ->
                val query = file.properties(includeNested = false)
                    .filter { it.name.endsWith("_QUERY") }
                    .singleOrNull()
                val fetch = file.functions(includeNested = false, includeLocal = false)
                    .filter { FETCH_FUNCTION_REGEX.containsMatchIn(it.name) }
                    .singleOrNull()
                val valid = query != null &&
                    query.hasInternalModifier &&
                    query.hasValModifier &&
                    fetch != null &&
                    fetch.hasInternalModifier &&
                    fetch.receiverType?.name?.substringBefore('<') == "GitHubClient"
                file.path.takeUnless { valid }
            }

        assertNoViolations(violations, SERVER_RULE, GITHUB_PROFILE_SOURCE_REFERENCE)
    }

    @Test
    fun kotlinxDatetimeStaysOutOfTheServer() {
        val violations = serverScope.files
            .flatMap { it.imports }
            .filter { it.name.startsWith(KOTLINX_DATETIME_PACKAGE) }
            .map { it.location }

        assertNoViolations(violations, SERVER_RULE, GITHUB_PROFILE_SOURCE_REFERENCE)
    }

    companion object {
        private const val SERVER_RULE = ".claude/rules/server.md"
        private const val PROFILE_ROUTES_REFERENCE =
            "server/src/main/kotlin/io/github/kei_1111/server/routing/ProfileRoutes.kt"
        private const val SERIALIZATION_PLUGIN_REFERENCE =
            "server/src/main/kotlin/io/github/kei_1111/server/plugins/Serialization.kt"
        private const val GITHUB_PROFILE_SOURCE_REFERENCE =
            "server/src/main/kotlin/io/github/kei_1111/server/client/GitHubProfileSource.kt"
        private const val KOTLINX_DATETIME_PACKAGE = "kotlinx.datetime"
        private val ROUTES_FILE_REGEX = Regex("/routing/[^/]+Routes\\.kt$")
        private val CONFIGURE_FUNCTION_REGEX = Regex("^configure[A-Z]")
        private val TTL_CACHE_CALL_REGEX = Regex("TtlCache(?:<[^(]*>)?\\(")
        private val NAMED_TTL_CACHE_CALL_REGEX = Regex("TtlCache(?:<[^(]*>)?\\([^)]*name\\s*=")
        private val GITHUB_SOURCE_FILE_REGEX = Regex("/client/GitHub[^/]*Source\\.kt$")
        private val FETCH_FUNCTION_REGEX = Regex("^fetch[A-Z]")
    }
}
