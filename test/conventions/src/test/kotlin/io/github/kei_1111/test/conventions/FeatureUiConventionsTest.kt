package io.github.kei_1111.test.conventions

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoSourceDeclaration
import com.lemonappdev.konsist.api.provider.KoLocationProvider
import com.lemonappdev.konsist.api.provider.KoNameProvider
import com.lemonappdev.konsist.api.provider.modifier.KoVisibilityModifierProvider
import org.junit.jupiter.api.Test

class FeatureUiConventionsTest {

    @Test
    fun intentMembersNeverUseOperationBasedClickNames() {
        val violations = destinationScope.interfaces(includeNested = false)
            .filter {
                it.name.endsWith("Intent") &&
                    it.hasSealedModifier &&
                    "/destination/" in it.path.normalizedPath
            }
            .flatMap { intent ->
                intent.classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
            }
            .filter { OPERATION_CLICK_NAME_REGEX.matches(it.name) }
            .map { it.location }

        assertNoViolations(
            violations,
            ".claude/rules/naming-conventions.md — Intent / Effect",
            PROFILE_INTENT_REFERENCE,
        )
    }

    @Test
    fun featureDeclarationsStayInternalOutsideNavigation() {
        val featureFiles = Konsist.scopeFromDirectory("app/feature").files
            .filter {
                val path = it.path.normalizedPath
                "/commonTest/" !in path && "/navigation/" !in path
            }
        val violations = featureFiles.flatMap { file ->
            file.declarations(includeNested = false, includeLocal = false)
                .filterIsInstance<KoSourceDeclaration>()
                .mapNotNull { declaration ->
                    val visibility = declaration as? KoVisibilityModifierProvider
                    declarationLocation(declaration, file).takeUnless {
                        visibility?.hasInternalModifier == true || visibility?.hasPrivateModifier == true
                    }
                }
        }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Destination Isolation — MUST",
            PROFILE_SCREEN_REFERENCE,
        )
    }

    @Test
    fun webAppPublicSurfaceIsExactlyTheAppEntrySet() {
        val publicDeclarations = Konsist.scopeFromDirectory("app/webApp").files
            .filter { MAIN_SOURCE_PATH_REGEX.containsMatchIn(it.path.normalizedPath) }
            .flatMap { file ->
                file.declarations(includeNested = false, includeLocal = false)
                    .filterIsInstance<KoSourceDeclaration>()
                    .mapNotNull { declaration ->
                        val visibility = declaration as? KoVisibilityModifierProvider
                        if (visibility?.hasInternalModifier == true || visibility?.hasPrivateModifier == true) {
                            null
                        } else {
                            declaration.name to declarationLocation(declaration, file)
                        }
                    }
            }
        val publicNames = publicDeclarations.map { it.first }.toSet()
        val violations = publicDeclarations
            .filter { it.first !in APP_ENTRY_PUBLIC_NAMES }
            .map { it.second } +
            (APP_ENTRY_PUBLIC_NAMES - publicNames).map { "app/webApp: missing $it" }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Destination Isolation — MUST",
            APP_REFERENCE,
        )
    }

    @Test
    fun rootComposablesOrderViewModelNavigationCallbacksModifier() {
        val violations = destinationScope.files
            .filter {
                val path = it.path.normalizedPath
                path.endsWith("ScreenRoot.kt") || path.endsWith("DialogRoot.kt")
            }
            .mapNotNull { file ->
                val root = file.composableNamedAfterFile()
                val parameters = root?.parameters.orEmpty()
                val hasModifierLast = parameters.lastOrNull()?.name == "modifier"
                val middleParameters = parameters.drop(1).let {
                    if (hasModifierLast) it.dropLast(1) else it
                }
                val firstNonNavigationIndex = middleParameters.indexOfFirst {
                    !it.name.startsWith("navigate")
                }
                val navigationOrderIsValid = firstNonNavigationIndex == -1 ||
                    middleParameters.drop(firstNonNavigationIndex).none { it.name.startsWith("navigate") }
                val modifierIsValid = hasModifierLast || root?.name in KNOWN_ROOT_PARAMETER_VIOLATIONS
                val valid = root != null &&
                    parameters.firstOrNull()?.name == "viewModel" &&
                    modifierIsValid &&
                    navigationOrderIsValid
                file.path.takeUnless { valid }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Screen Structure (MVI + Breakpoint-Branching)",
            PROFILE_ROOT_REFERENCE,
        )
    }

    @Test
    fun screenAndContentComposablesTakeStateThenOnIntent() {
        val violations = destinationScope.files
            .filter { file ->
                val path = file.path.normalizedPath
                val isScreenOrDialog = !path.endsWith("Root.kt") &&
                    (path.endsWith("Screen.kt") || path.endsWith("Dialog.kt"))
                val isContent = "/content/" in path && path.endsWith("Content.kt")
                isScreenOrDialog || isContent
            }
            .mapNotNull { file ->
                val composable = file.composableNamedAfterFile()
                val parameters = composable?.parameters.orEmpty()
                val onIntentIndex = parameters.indexOfFirst { it.name == "onIntent" }
                val valid = composable != null &&
                    parameters.firstOrNull()?.name == "state" &&
                    parameters.lastOrNull()?.name == "modifier" &&
                    (onIntentIndex == -1 || onIntentIndex == 1)
                file.path.takeUnless { valid }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Screen Structure (MVI + Breakpoint-Branching)",
            PROFILE_SCREEN_REFERENCE,
        )
    }

    @Test
    fun testTagAndStringResourceStayInComponentFiles() {
        val violations = appAndDestinationCommonMainFiles()
            .filter { "/component/" !in it.path.normalizedPath }
            .mapNotNull { file ->
                file.path.takeIf { ".testTag(" in file.text || "stringResource(" in file.text }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Component Responsibilities",
            TITLE_BAR_REFERENCE,
        )
    }

    @Test
    fun collectAsStateWithLifecycleStaysAtTheRoot() {
        val violations = appAndDestinationCommonMainFiles()
            .filter { "collectAsStateWithLifecycle(" in it.text }
            .mapNotNull { file ->
                val valid = file.path.normalizedPath.endsWith("Root.kt") &&
                    "viewModel.state.collectAsStateWithLifecycle(" in file.text
                file.path.takeUnless { valid }
            }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — File Structure",
            PROFILE_ROOT_REFERENCE,
        )
    }

    @Test
    fun destinationThemeObjectsAreInternalDataObjects() {
        val violations = destinationScope.files
            .filter { DESTINATION_THEME_PATH_REGEX.containsMatchIn(it.path.normalizedPath) }
            .flatMap { it.objects(includeNested = false) }
            .mapNotNull { themeObject ->
                val propertiesArePascalCase = themeObject.properties(includeNested = false)
                    .all { it.name.firstOrNull()?.isUpperCase() == true }
                themeObject.location.takeUnless {
                    themeObject.hasInternalModifier && themeObject.hasDataModifier && propertiesArePascalCase
                }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — `destination/<name>/` Directory Layout",
            PROFILE_DIMENSIONS_REFERENCE,
        )
    }

    @Test
    fun previewFixturesAreInternalTopLevelPreviewVals() {
        val violations = destinationScope.files
            .filter { DESTINATION_PREVIEW_PATH_REGEX.containsMatchIn(it.path.normalizedPath) }
            .flatMap { it.properties(includeNested = false) }
            .mapNotNull { fixture ->
                fixture.location.takeUnless {
                    fixture.hasInternalModifier && fixture.hasValModifier && fixture.name.startsWith("Preview")
                }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — `destination/<name>/` Directory Layout",
            PROFILE_PREVIEW_FIXTURES_REFERENCE,
        )
    }

    @Test
    fun material3ImportsStayOnThePrimitiveAllowlist() {
        val violations = appAndDestinationFiles()
            .flatMap { it.imports }
            .filter { it.name.startsWith(MATERIAL3_PACKAGE_PREFIX) }
            .mapNotNull { import ->
                val importedName = import.name.substringAfterLast('.')
                import.location.takeUnless {
                    !import.isWildcard && importedName in MATERIAL3_PRIMITIVE_ALLOWLIST
                }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — IDE Design Rules (Islands Dark / Light)",
            APP_REFERENCE,
        )
    }

    @Test
    fun globalScopeAndPrintlnNeverAppear() {
        val sharedAndServerScope = Konsist.scopeFromDirectory("shared/model", "server")
        val violations = (appScope.files + destinationScope.files + sharedAndServerScope.files)
            .filter { MAIN_SOURCE_PATH_REGEX.containsMatchIn(it.path.normalizedPath) }
            .distinctBy { it.path.normalizedPath }
            .mapNotNull { file ->
                file.path.takeIf { "GlobalScope" in file.text || "println(" in file.text }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Component Responsibilities",
            PROFILE_VIEW_MODEL_REFERENCE,
        )
    }

    @Test
    fun dispatchersReferencesAreConfinedToTheProviderAndTestBase() {
        val violations = appScope.files
            .filter { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath && "Dispatchers." in it.text }
            .mapNotNull { file ->
                file.path.takeUnless { path ->
                    DISPATCHERS_ALLOWED_PATH_SUFFIXES.any { path.normalizedPath.endsWith(it) }
                }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Component Responsibilities",
            DISPATCHER_BINDINGS_REFERENCE,
        )
    }

    @Test
    fun viewModelScopeIsImportedOnlyByViewModels() {
        val violations = appScope.files
            .filter { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }
            .filter { file -> file.imports.any { it.name == VIEW_MODEL_SCOPE_IMPORT } }
            .mapNotNull { file ->
                file.path.takeUnless { it.normalizedPath.endsWith("ViewModel.kt") }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Screen Structure (MVI + Breakpoint-Branching)",
            PROFILE_VIEW_MODEL_REFERENCE,
        )
    }

    @Test
    fun compositionLocalsAreDefinedInCoreAndNamedLocal() {
        val declarationViolations = appAndDestinationCommonMainFiles()
            .flatMap { it.properties(includeNested = false) }
            .filter {
                "compositionLocalOf" in it.text || "staticCompositionLocalOf" in it.text
            }
            .mapNotNull { property ->
                val valid = "/app/core/" in property.path.normalizedPath && property.name.startsWith("Local")
                property.location.takeUnless { valid }
            }
        val providerViolations = destinationScope.files
            .filter { COMPOSITION_LOCAL_PROVIDES_REGEX.containsMatchIn(it.text) }
            .map { it.path }

        assertNoViolations(
            declarationViolations + providerViolations,
            ".claude/rules/naming-conventions.md — Composable",
            KEI_THEME_REFERENCE,
        )
    }

    @Test
    fun topLevelConstValsAreNeverPublic() {
        val violations = appAndDestinationFiles()
            .flatMap { it.properties(includeNested = false) }
            .filter { it.hasConstModifier }
            .mapNotNull { property ->
                property.location.takeUnless { property.hasPrivateModifier || property.hasInternalModifier }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — Destination Isolation — MUST",
            PROFILE_VIEW_MODEL_REFERENCE,
        )
    }

    @Test
    fun designSystemTokenClassesAreImmutable() {
        val violations = appScope.classes(includeNested = true, includeLocal = false)
            .filter {
                it.hasDataModifier && DESIGN_SYSTEM_THEME_PATH_REGEX.containsMatchIn(it.path.normalizedPath)
            }
            .mapNotNull { tokenClass ->
                tokenClass.location.takeUnless { tokenClass.hasAnnotationWithName("Immutable") }
            }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — IDE Design Rules (Islands Dark / Light)",
            KEI_COLOR_SCHEME_REFERENCE,
        )
    }

    @Test
    fun keiThemeAccessorsAreReadOnlyComposableGetters() {
        val keiTheme = appScope.objects(includeNested = false)
            .singleOrNull {
                it.name == "KeiTheme" && it.path.normalizedPath.endsWith("/theme/KeiTheme.kt")
            }
        val violations = if (keiTheme == null) {
            listOf(KEI_THEME_REFERENCE)
        } else {
            keiTheme.properties(includeNested = false).mapNotNull { property ->
                val valid = "@Composable" in property.text &&
                    "@ReadOnlyComposable" in property.text &&
                    "get() =" in property.text
                property.location.takeUnless { valid }
            }
        }

        assertNoViolations(
            violations,
            ".claude/rules/ui-implementation.md — IDE Design Rules (Islands Dark / Light)",
            KEI_THEME_REFERENCE,
        )
    }

    @Test
    fun previewsArePrivateBareAndSuffixNamed() {
        val previewViolations = previewFunctions().mapNotNull { function ->
            val previewAnnotation = function.annotations.singleOrNull { it.name == "Preview" }
            val valid = function.hasPrivateModifier &&
                function.name.endsWith("Preview") &&
                previewAnnotation?.text == "@Preview"
            function.location.takeUnless { valid }
        }
        val infrastructureViolations = (
            appScope.declarations(includeNested = true, includeLocal = false) +
                destinationScope.declarations(includeNested = true, includeLocal = false)
            )
            .mapNotNull { declaration ->
                val name = (declaration as? KoNameProvider)?.name
                val location = (declaration as? KoLocationProvider)?.location
                location?.takeIf { name in FORBIDDEN_PREVIEW_DECLARATION_NAMES }
            }
            .distinct()

        assertNoViolations(
            previewViolations + infrastructureViolations,
            PREVIEW_RULE,
            TITLE_BAR_REFERENCE,
        )
    }

    @Test
    fun testTagsNeverInlineStringLiterals() {
        val violations = (appScope.files + destinationScope.files)
            .distinctBy { it.path.normalizedPath }
            .mapNotNull { file ->
                file.path.takeIf { INLINE_TEST_TAG_REGEX.containsMatchIn(file.text) }
            }

        assertNoViolations(
            violations,
            ".claude/rules/naming-conventions.md — testTag",
            TITLE_BAR_REFERENCE,
        )
    }

    private fun previewFunctions() = (
        appScope
            .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }
            .functions(includeNested = true, includeLocal = false) +
            destinationScope.functions(includeNested = true, includeLocal = false)
        )
        .filter { it.hasAnnotationWithName("Preview") }
        .distinctBy { it.location }

    private fun appAndDestinationFiles(): List<KoFileDeclaration> = (appScope.files + destinationScope.files)
        .distinctBy { it.path.normalizedPath }

    private fun appAndDestinationCommonMainFiles(): List<KoFileDeclaration> = appAndDestinationFiles()
        .filter { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }

    private fun KoFileDeclaration.composableNamedAfterFile() =
        functions(includeNested = false, includeLocal = false).singleOrNull {
            it.name == path.normalizedPath.substringAfterLast('/').removeSuffix(".kt") &&
                it.hasAnnotationWithName("Composable")
        }

    private fun declarationLocation(declaration: KoSourceDeclaration, file: KoFileDeclaration): String =
        (declaration as? KoLocationProvider)?.location ?: "${file.path}: ${declaration.name}"

    companion object {
        private const val PREVIEW_RULE = ".claude/rules/preview.md"
        private const val APP_REFERENCE =
            "app/webApp/src/commonMain/kotlin/io/github/kei_1111/app/App.kt"
        private const val PROFILE_INTENT_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/ProfileIntent.kt"
        private const val PROFILE_SCREEN_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/ProfileScreen.kt"
        private const val PROFILE_ROOT_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/ProfileScreenRoot.kt"
        private const val PROFILE_VIEW_MODEL_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/ProfileViewModel.kt"
        private const val PROFILE_DIMENSIONS_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/theme/ProfileDimensions.kt"
        private const val PROFILE_PREVIEW_FIXTURES_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/preview/ProfilePreviewFixtures.kt"
        private const val TITLE_BAR_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile/component/TitleBar.kt"
        private const val KEI_THEME_REFERENCE =
            "app/core/designsystem/src/commonMain/kotlin/io/github/kei_1111/app/core/designsystem/theme/KeiTheme.kt"
        private const val KEI_COLOR_SCHEME_REFERENCE =
            "app/core/designsystem/src/commonMain/kotlin/io/github/kei_1111/app/core/designsystem/theme/KeiColorScheme.kt"
        private const val DISPATCHER_BINDINGS_REFERENCE =
            "app/core/common/src/commonMain/kotlin/io/github/kei_1111/app/core/common/dispatcher/DispatcherBindings.kt"
        private const val MATERIAL3_PACKAGE_PREFIX = "androidx.compose.material3."
        private const val VIEW_MODEL_SCOPE_IMPORT = "androidx.lifecycle.viewModelScope"
        private val KNOWN_ROOT_PARAMETER_VIOLATIONS = setOf("GoldenDialogDialogRoot")
        private val OPERATION_CLICK_NAME_REGEX = Regex("^On[A-Z].*Click$")
        private val MAIN_SOURCE_PATH_REGEX = Regex("/src/(?:main|[^/]*Main)/")
        private val DESTINATION_THEME_PATH_REGEX = Regex("/destination/[^/]+/theme/")
        private val DESTINATION_PREVIEW_PATH_REGEX = Regex("/destination/[^/]+/preview/")
        private val DESIGN_SYSTEM_THEME_PATH_REGEX = Regex("/app/core/designsystem/.*/theme/")
        private val APP_ENTRY_PUBLIC_NAMES = setOf("App", "AppNavDisplay", "AppGraph", "main")
        private val MATERIAL3_PRIMITIVE_ALLOWLIST =
            setOf("Text", "Icon", "HorizontalDivider", "VerticalDivider", "Surface")
        private val DISPATCHERS_ALLOWED_PATH_SUFFIXES = setOf(
            "/app/core/common/src/commonMain/kotlin/io/github/kei_1111/app/core/common/dispatcher/DispatcherBindings.kt",
            "/app/core/testing/src/commonMain/kotlin/io/github/kei_1111/app/core/testing/ViewModelTestBase.kt",
        )
        private val FORBIDDEN_PREVIEW_DECLARATION_NAMES =
            setOf("ComponentPreviews", "ScreenPreviews", "PreviewWrapper")
        private val INLINE_TEST_TAG_REGEX = Regex("\\.testTag\\(\\s*\"")
        private val COMPOSITION_LOCAL_PROVIDES_REGEX = Regex("\\bLocal\\w+\\s+provides\\b")
    }
}
