package io.github.kei_1111.test.conventions

import org.junit.jupiter.api.Test

class SharedModelConventionsTest {

    @Test
    fun serializableConstructorPropertiesPinSerialNameToThePropertyName() {
        val violations = serializableClasses()
            .filterNot { it.hasValueModifier }
            .flatMap { serializableClass ->
                serializableClass.primaryConstructor
                    ?.parameters
                    .orEmpty()
                    .filter { it.hasValModifier || it.hasVarModifier }
            }
            .mapNotNull { property ->
                val serialName = property.annotations.singleOrNull { it.name == "SerialName" }
                property.location.takeUnless {
                    serialName?.text?.contains("\"${property.name}\"") == true
                }
            }

        assertNoViolations(violations, SHARED_MODEL_RULE, PROFILE_REFERENCE)
    }

    @Test
    fun sharedModelTypesAreSerializableValOnlyAndFunctionFree() {
        val violations = sharedModelScope
            .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
            .filter {
                val path = it.path.normalizedPath
                "/serialization/" !in path && !path.endsWith("/License.kt")
            }
            .mapNotNull { type ->
                val hasVarProperty = type.properties(includeNested = true).any { it.hasVarModifier }
                val hasMemberFunction = type.functions(includeNested = true, includeLocal = false).isNotEmpty()
                type.location.takeUnless {
                    type.hasAnnotationWithName("Serializable") && !hasVarProperty && !hasMemberFunction
                }
            }

        assertNoViolations(violations, SHARED_MODEL_RULE, PROFILE_REFERENCE)
    }

    @Test
    fun nullableSerializablePropertiesDefaultToNull() {
        val violations = serializableClasses().flatMap { serializableClass ->
            serializableClass.primaryConstructor
                ?.parameters
                .orEmpty()
                .filter { it.type.isNullable }
                .mapNotNull { property ->
                    property.location.takeUnless { property.defaultValue == "null" }
                }
        }

        assertNoViolations(violations, SHARED_MODEL_RULE, PROFILE_REFERENCE)
    }

    @Test
    fun kotlinxDatetimeStaysOut() {
        val violations = sharedModelScope.files
            .flatMap { it.imports }
            .filter { it.name.startsWith(KOTLINX_DATETIME_PACKAGE) }
            .map { it.location }

        assertNoViolations(violations, SHARED_MODEL_RULE, PROFILE_REFERENCE)
    }

    private fun serializableClasses() = sharedModelScope
        .classes(includeNested = true, includeLocal = false)
        .filter { it.hasAnnotationWithName("Serializable") }

    companion object {
        private const val SHARED_MODEL_RULE = ".claude/rules/shared-model.md"
        private const val PROFILE_REFERENCE =
            "shared/model/src/commonMain/kotlin/io/github/kei_1111/shared/model/Profile.kt"
        private const val KOTLINX_DATETIME_PACKAGE = "kotlinx.datetime"
    }
}
