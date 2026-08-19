package io.github.kei_1111.test.conventions

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import org.junit.jupiter.api.Assertions.assertTrue

internal fun assertNoViolations(
    violations: List<String>,
    rule: String,
    reference: String,
) {
    assertTrue(violations.isEmpty(), failureMessage(rule, reference, violations))
}

internal fun failureMessage(
    rule: String,
    reference: String,
    violations: List<String>,
): String = buildString {
    append("Violates $rule; copy from $reference")
    if (violations.isNotEmpty()) {
        append("\n")
        append(violations.joinToString("\n"))
    }
}

internal val String.normalizedPath: String
    get() = replace('\\', '/')

internal fun KoClassDeclaration.hasParentTypeNamed(name: String): Boolean =
    parents().any { it.name.substringBefore('<') == name }

internal val domainScope = Konsist
    .scopeFromDirectory("app/core/domain")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val dataScope = Konsist
    .scopeFromDirectory("app/core/data")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val apiScope = Konsist
    .scopeFromDirectory("app/core/api")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val localScope = Konsist
    .scopeFromDirectory("app/core/local")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val appScope = Konsist.scopeFromDirectory("app")

internal val sharedModelScope = Konsist
    .scopeFromDirectory("shared/model")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val serverScope = Konsist
    .scopeFromDirectory("server")
    .slice { SERVER_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val clientTestScope = Konsist
    .scopeFromDirectory("app")
    .slice { COMMON_TEST_PATH_SEGMENT in it.path.normalizedPath }

internal val sharedModelTestScope = Konsist
    .scopeFromDirectory("shared/model")
    .slice { COMMON_TEST_PATH_SEGMENT in it.path.normalizedPath }

internal val serverTestScope = Konsist
    .scopeFromDirectory("server")
    .slice { SERVER_TEST_PATH_SEGMENT in it.path.normalizedPath }

internal val e2eScope = Konsist.scopeFromDirectory("test/e2e")

internal val destinationScope = Konsist
    .scopeFromDirectory("app/feature", "template")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal val coreMviScope = Konsist
    .scopeFromDirectory("app/core/mvi")
    .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

internal const val COMMON_MAIN_PATH_SEGMENT = "/src/commonMain/"
internal const val COMMON_TEST_PATH_SEGMENT = "/src/commonTest/"
internal const val SERVER_MAIN_PATH_SEGMENT = "/src/main/"
internal const val SERVER_TEST_PATH_SEGMENT = "/src/test/"
