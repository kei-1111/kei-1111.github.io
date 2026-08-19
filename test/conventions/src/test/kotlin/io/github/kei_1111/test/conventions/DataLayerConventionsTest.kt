package io.github.kei_1111.test.conventions

import org.junit.jupiter.api.Test

class DataLayerConventionsTest {

    @Test
    fun bindingImplsCarryTheMetroTrioInOrder() {
        val violations = bindingImpls().mapNotNull { implementation ->
            val classIndex = implementation.text.indexOf("class ")
            val classAnnotations = implementation.text.substringBefore("class ")
            val contributesBindingIndex = classAnnotations.indexOf("@ContributesBinding")
            val singleInIndex = classAnnotations.indexOf("@SingleIn")
            val injectIndex = classAnnotations.indexOf("@Inject")
            val valid = if (implementation.name == NOTIFICATION_LOCAL_DATA_SOURCE_IMPL) {
                implementation.hasAnnotationsWithAllNames("ContributesBinding", "SingleIn") &&
                    contributesBindingIndex >= 0 &&
                    singleInIndex > contributesBindingIndex &&
                    !implementation.hasAnnotationWithName("Inject") &&
                    classIndex >= 0 &&
                    implementation.text.indexOf("@Inject", startIndex = classIndex) > classIndex
            } else {
                implementation.hasAnnotationsWithAllNames("ContributesBinding", "SingleIn", "Inject") &&
                    contributesBindingIndex >= 0 &&
                    singleInIndex > contributesBindingIndex &&
                    injectIndex > singleInIndex
            }
            implementation.location.takeUnless { valid }
        }

        assertNoViolations(
            violations,
            ".claude/rules/data-layer.md; .claude/rules/usecase.md",
            DATA_LAYER_REFERENCE,
        )
    }

    @Test
    fun repositoryStreamsAreValFlowProperties() {
        val violations = repositoryInterfaces().mapNotNull { repository ->
            val propertiesAreFlows = repository
                .properties(includeNested = false)
                .all { it.type?.name?.substringBefore('<') == "Flow" && !it.hasVarModifier }
            val functions = repository.functions(includeNested = false, includeLocal = false)
            val hasGetterFunction = functions.any { GETTER_NAME_REGEX.containsMatchIn(it.name) }
            val hasFlowFunction = functions.any { it.returnType?.name?.substringBefore('<') == "Flow" }
            repository.location.takeUnless {
                propertiesAreFlows && !hasGetterFunction && !hasFlowFunction
            }
        }

        assertNoViolations(violations, DATA_LAYER_RULE, DATA_LAYER_REFERENCE)
    }

    @Test
    fun repositoryImplsNeverTouchHttpClient() {
        val violations = dataScope.classes(includeNested = false, includeLocal = false)
            .filter { it.name.endsWith("RepositoryImpl") }
            .mapNotNull { implementation ->
                val touchesHttpClient = implementation.primaryConstructor
                    ?.parameters
                    .orEmpty()
                    .any { it.type?.name?.substringBefore('<') == "HttpClient" }
                implementation.location.takeIf { touchesHttpClient }
            }

        assertNoViolations(violations, DATA_LAYER_RULE, DATA_LAYER_REFERENCE)
    }

    @Test
    fun apiInterfacesDeclareExactlyOneNullableFetch() {
        val interfaceViolations = apiScope.interfaces(includeNested = false)
            .filter { it.name.endsWith("Api") }
            .mapNotNull { api ->
                val fetch = api.functions(includeNested = false, includeLocal = false).singleOrNull()
                val expectedFetchName = "fetch${api.name.removeSuffix("Api")}"
                val valid = fetch != null &&
                    fetch.hasSuspendModifier &&
                    FETCH_FUNCTION_NAME_REGEX.containsMatchIn(fetch.name) &&
                    fetch.name == expectedFetchName &&
                    fetch.parameters.isEmpty() &&
                    fetch.returnType?.text?.endsWith("?") == true
                api.location.takeUnless { valid }
            }
        val implementationViolations = apiScope.classes(includeNested = false, includeLocal = false)
            .filter { it.name.endsWith("ApiImpl") }
            .mapNotNull { implementation ->
                val client = implementation.properties(includeNested = false).singleOrNull()
                val valid = client != null &&
                    client.hasPrivateModifier &&
                    client.hasValModifier &&
                    client.name == "client" &&
                    client.type?.name?.substringBefore('<') == "HttpClient"
                implementation.location.takeUnless { valid }
            }

        assertNoViolations(
            interfaceViolations + implementationViolations,
            DATA_LAYER_RULE,
            API_REFERENCE,
        )
    }

    @Test
    fun repositoryImplsTakeTheDispatcherFirst() {
        val violations = repositoryImplsWithParameters().mapNotNull { implementation ->
            val firstTypeName = implementation.primaryConstructor
                ?.parameters
                ?.firstOrNull()
                ?.type
                ?.name
                ?.substringBefore('<')
            implementation.location.takeUnless { firstTypeName == "CoroutineDispatcher" }
        }

        assertNoViolations(violations, DATA_LAYER_RULE, DATA_LAYER_REFERENCE)
    }

    @Test
    fun injectedParameterNamesMirrorTheirTypes() {
        val violations = repositoryImplsWithParameters().flatMap { implementation ->
            implementation.primaryConstructor
                ?.parameters
                .orEmpty()
                .filter { it.type?.name?.substringBefore('<') != "CoroutineDispatcher" }
                .mapNotNull { parameter ->
                    val typeName = parameter.type?.name?.substringBefore('<')
                    val expectedName = typeName?.replaceFirstChar { it.lowercase() }
                    parameter.location.takeUnless { expectedName != null && parameter.name == expectedName }
                }
        }

        assertNoViolations(violations, DATA_LAYER_RULE, DATA_LAYER_REFERENCE)
    }

    @Test
    fun bindingPairFilesAreNamedAfterTheInterface() {
        val violations = bindingScopesFiles().flatMap { file ->
            val implementationNames = file.classes(includeNested = false, includeLocal = false)
                .map { it.name }
                .toSet()
            file.interfaces(includeNested = false)
                .filter { bindingName -> BINDING_INTERFACE_SUFFIXES.any { bindingName.name.endsWith(it) } }
                .filter { "${it.name}Impl" in implementationNames }
                .mapNotNull { binding ->
                    val fileName = file.path.normalizedPath.substringAfterLast('/')
                    file.path.takeUnless { fileName == "${binding.name}.kt" }
                }
        }

        assertNoViolations(violations, DATA_LAYER_RULE, DATA_LAYER_REFERENCE)
    }

    @Test
    fun dataAndDomainPackagesStayFlat() {
        val dataViolations = dataScope.files
            .filter { "/data/repository/" in it.path.normalizedPath }
            .mapNotNull { file ->
                file.path.takeUnless { DATA_REPOSITORY_FILE_REGEX.containsMatchIn(it.normalizedPath) }
            }
        val domainViolations = domainScope.files
            .filter { "/domain/usecase/" in it.path.normalizedPath }
            .mapNotNull { file ->
                file.path.takeUnless { DOMAIN_USE_CASE_FILE_REGEX.containsMatchIn(it.normalizedPath) }
            }
        val apiViolations = apiScope.files.mapNotNull { file ->
            file.path.takeUnless { file.packagee?.name.hasExactlyOneSegmentBelow(API_PACKAGE_ROOT) }
        }
        val localViolations = localScope.files.mapNotNull { file ->
            file.path.takeUnless { file.packagee?.name.hasExactlyOneSegmentBelow(LOCAL_PACKAGE_ROOT) }
        }

        assertNoViolations(
            dataViolations + domainViolations + apiViolations + localViolations,
            DATA_LAYER_RULE,
            DATA_LAYER_REFERENCE,
        )
    }

    @Test
    fun providesFunctionsOrderQualifierProvidesScope() {
        val providesFunctions = (
            appScope.functions(includeNested = true, includeLocal = false) +
                destinationScope.functions(includeNested = true, includeLocal = false)
            )
            .filter { it.hasAnnotationWithName("Provides") }
            .distinctBy { it.location }
        val violations = providesFunctions.mapNotNull { function ->
            val functionPrefix = function.text.substringBefore("fun ")
            val providesIndex = functionPrefix.indexOf("@Provides")
            val qualifierAnnotations = function.annotations.filter { it.name !in PROVIDES_ORDER_ANNOTATIONS }
            val scopeAnnotations = function.annotations.filter { it.name in PROVIDES_SCOPE_ANNOTATIONS }
            val qualifiersAreBeforeProvides = qualifierAnnotations.all {
                val index = functionPrefix.indexOf("@${it.name}")
                index >= 0 && index < providesIndex
            }
            val scopesAreAfterProvides = scopeAnnotations.all {
                functionPrefix.indexOf("@${it.name}") > providesIndex
            }
            function.location.takeUnless {
                providesIndex >= 0 && qualifiersAreBeforeProvides && scopesAreAfterProvides
            }
        }

        assertNoViolations(violations, DATA_LAYER_RULE, DISPATCHER_BINDINGS_REFERENCE)
    }

    @Test
    fun resultTypesNeverAppearInDataOrDomainSignatures() {
        val files = (dataScope.files + domainScope.files).distinctBy { it.path.normalizedPath }
        val importViolations = files
            .flatMap { it.imports }
            .filter { it.name.endsWith(".Result") }
            .map { it.location }
        val functionViolations = (
            dataScope.functions(includeNested = true, includeLocal = false) +
                domainScope.functions(includeNested = true, includeLocal = false)
            ).flatMap { function ->
            val returnViolation = function.location.takeIf {
                function.returnType?.name?.substringBefore('<') == "Result"
            }
            val parameterViolations = function.parameters.mapNotNull { parameter ->
                parameter.location.takeIf { parameter.type.name.substringBefore('<') == "Result" }
            }
            listOfNotNull(returnViolation) + parameterViolations
        }
        val propertyViolations = (
            dataScope.properties(includeNested = true) +
                domainScope.properties(includeNested = true)
            ).mapNotNull { property ->
            property.location.takeIf { property.type?.name?.substringBefore('<') == "Result" }
        }

        assertNoViolations(
            (importViolations + functionViolations + propertyViolations).distinct(),
            ".claude/rules/error-handling.md — Prohibited Patterns",
            DATA_LAYER_REFERENCE,
        )
    }

    private fun bindingImpls() = (
        apiScope.classes(includeNested = false, includeLocal = false) +
            dataScope.classes(includeNested = false, includeLocal = false) +
            localScope.classes(includeNested = false, includeLocal = false) +
            domainScope.classes(includeNested = false, includeLocal = false)
        )
        .filter { implementation ->
            BINDING_IMPL_SUFFIXES.any { implementation.name.endsWith(it) }
        }

    private fun repositoryInterfaces() = dataScope.interfaces(includeNested = false)
        .filter { it.name.endsWith("Repository") }

    private fun repositoryImplsWithParameters() = dataScope.classes(includeNested = false, includeLocal = false)
        .filter {
            it.name.endsWith("RepositoryImpl") && it.primaryConstructor?.parameters.orEmpty().isNotEmpty()
        }

    private fun bindingScopesFiles() = (apiScope.files + dataScope.files + localScope.files + domainScope.files)
        .distinctBy { it.path.normalizedPath }

    private fun String?.hasExactlyOneSegmentBelow(root: String): Boolean {
        if (this == null || !startsWith("$root.")) return false
        val relativePackage = removePrefix("$root.")
        return relativePackage.isNotEmpty() && '.' !in relativePackage
    }

    companion object {
        private const val DATA_LAYER_RULE = ".claude/rules/data-layer.md"
        private const val DATA_LAYER_REFERENCE =
            "app/core/data/src/commonMain/kotlin/io/github/kei_1111/app/core/data/repository/ProfileRepository.kt"
        private const val API_REFERENCE =
            "app/core/api/src/commonMain/kotlin/io/github/kei_1111/app/core/api/profile/ProfileApi.kt"
        private const val DISPATCHER_BINDINGS_REFERENCE =
            "app/core/common/src/commonMain/kotlin/io/github/kei_1111/app/core/common/dispatcher/DispatcherBindings.kt"
        private const val API_PACKAGE_ROOT = "io.github.kei_1111.app.core.api"
        private const val LOCAL_PACKAGE_ROOT = "io.github.kei_1111.app.core.local"
        private const val NOTIFICATION_LOCAL_DATA_SOURCE_IMPL = "NotificationLocalDataSourceImpl"
        private val BINDING_IMPL_SUFFIXES = setOf("RepositoryImpl", "UseCaseImpl", "ApiImpl", "DataSourceImpl")
        private val BINDING_INTERFACE_SUFFIXES = setOf("Repository", "UseCase", "Api", "DataSource")
        private val GETTER_NAME_REGEX = Regex("^get[A-Z]")
        private val FETCH_FUNCTION_NAME_REGEX = Regex("^fetch[A-Z]")
        private val DATA_REPOSITORY_FILE_REGEX = Regex("/data/repository/[^/]+\\.kt$")
        private val DOMAIN_USE_CASE_FILE_REGEX = Regex("/domain/usecase/[^/]+\\.kt$")
        private val PROVIDES_ORDER_ANNOTATIONS = setOf("Provides", "SingleIn", "IntoSet")
        private val PROVIDES_SCOPE_ANNOTATIONS = setOf("SingleIn", "IntoSet")
    }
}
