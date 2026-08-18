package io.github.kei_1111.test.conventions

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MviConventionsTest {

    @Test
    fun scopeSeesFeatureAndTemplateDestinations() {
        val paths = destinationScope.files.map { it.path.normalizedPath }
        val seesProfile = paths.any { it.endsWith("/destination/profile/ProfileViewModel.kt") }
        val seesTemplate = paths.any {
            it.endsWith("/template/destination/screen/GoldenViewModel.kt")
        }

        assertTrue(
            seesProfile && seesTemplate,
            failureMessage(
                rule = ".claude/rules/mvi-architecture.md — File Structure",
                reference = PROFILE_DESTINATION,
                violations = listOf("Architecture scope does not include both ProfileViewModel.kt and the screen GoldenViewModel.kt"),
            ),
        )
    }

    @Test
    fun destinationDeclaresTheCompleteMviFileSet() {
        val paths = destinationScope.files.map { it.path.normalizedPath }.toSet()
        val violations = paths
            .filter { DESTINATION_VIEW_MODEL_FILE_REGEX.containsMatchIn(it) }
            .flatMap { viewModelPath ->
                val directory = viewModelPath.substringBeforeLast('/')
                val destinationName = viewModelPath.substringAfterLast('/').removeSuffix("ViewModel.kt")
                REQUIRED_MVI_SUFFIXES.mapNotNull { suffix ->
                    val requiredPath = "$directory/$destinationName$suffix.kt"
                    requiredPath.takeUnless(paths::contains)
                }
            }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — File Structure",
            PROFILE_DESTINATION,
        )
    }

    @Test
    fun viewModelsCarryTheMetroMviContract() {
        val violations = destinationViewModelTypes().mapNotNull { viewModel ->
            val valid = viewModel is KoClassDeclaration &&
                viewModel.hasInternalModifier &&
                viewModel.hasParentTypeNamed("MviViewModel") &&
                viewModel.hasAnnotationsWithAllNames("Inject", "ViewModelKey", "ContributesIntoMap")
            viewModel.location.takeUnless { valid }
        }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — ViewModel Pattern (Metro)",
            "$PROFILE_DESTINATION/ProfileViewModel.kt",
        )
    }

    @Test
    fun intentsAreInternalSealedAndDeclareConsumeEffect() {
        val violations = destinationTypes()
            .filter { it.name.endsWith("Intent") && it.path.normalizedPath.contains("/destination/") }
            .mapNotNull { intent ->
                val valid = intent is KoInterfaceDeclaration &&
                    intent.hasInternalModifier &&
                    intent.hasSealedModifier &&
                    intent.hasParentWithName("Intent") &&
                    intent.hasObjectWithName("ConsumeEffect", includeNested = false)
                intent.location.takeUnless { valid }
            }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — File Structure; " +
                ".claude/rules/naming-conventions.md — Intent / Effect",
            "$PROFILE_DESTINATION/ProfileIntent.kt",
        )
    }

    @Test
    fun statesAreInternalDataClassesImplementingStateWithEffect() {
        val violations = destinationStateTypes().mapNotNull { state ->
            val expectedParent = if (state.name.endsWith("ViewModelState")) "ViewModelState" else "State"
            val valid = state is KoClassDeclaration &&
                state.hasInternalModifier &&
                state.hasDataModifier &&
                state.hasParentTypeNamed(expectedParent) &&
                state.hasPropertyWithName("effect", includeNested = false)
            state.location.takeUnless { valid }
        }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — File Structure",
            "$PROFILE_DESTINATION/ProfileState.kt",
        )
    }

    @Test
    fun effectsAreInternalSealedInterfacesOutsideCoreMvi() {
        val destinationViolations = destinationTypes()
            .filter { it.name.endsWith("Effect") }
            .mapNotNull { effect ->
                val valid = effect is KoInterfaceDeclaration &&
                    effect.hasInternalModifier &&
                    effect.hasSealedModifier
                effect.location.takeUnless { valid }
            }
        val coreViolations = coreMviScope.classesAndInterfacesAndObjects()
            .filter { it.name.endsWith("Effect") }
            .map { it.location }

        assertNoViolations(
            destinationViolations + coreViolations,
            ".claude/rules/mvi-architecture.md — Core Components",
            "$PROFILE_DESTINATION/ProfileEffect.kt",
        )
    }

    @Test
    fun stateFieldsUseNoMutableCollections() {
        val violations = destinationStateClasses().flatMap { state ->
            val declaredTypes = state.properties(includeNested = false).mapNotNull { it.type } +
                state.primaryConstructor?.parameters.orEmpty().mapNotNull { it.type }
            declaredTypes.mapNotNull { type ->
                val typeName = type.name.substringBefore('<')
                "${state.location}: $typeName".takeIf { typeName in MUTABLE_STATE_TYPE_NAMES }
            }.distinct()
        }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — File Structure",
            "$PROFILE_DESTINATION/ProfileState.kt",
        )
    }

    @Test
    fun onIntentNeverRedispatchesIntents() {
        val violations = onIntentFunctions().mapNotNull { function ->
            function.location.takeUnless { function.text.countOccurrences("onIntent(") == 1 }
        }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — onIntent Policy",
            "$PROFILE_DESTINATION/ProfileViewModel.kt",
        )
    }

    @Test
    fun componentsDoNotReferenceIntentTypes() {
        val violations = destinationScope.files
            .filter { COMPONENT_PATH_REGEX.containsMatchIn(it.path.normalizedPath) }
            .mapNotNull { file ->
                val hasIntentImport = INTENT_IMPORT_REGEX.containsMatchIn(file.text)
                val hasIntentReference = INTENT_REFERENCE_REGEX.containsMatchIn(file.text)
                file.path.takeIf { hasIntentImport || hasIntentReference }
            }

        assertNoViolations(
            violations,
            ".claude/rules/naming-conventions.md — Composable; " +
                ".claude/rules/ui-implementation.md — Component Responsibilities",
            "$PROFILE_DESTINATION/component/TitleBar.kt",
        )
    }

    @Test
    fun effectReadingRootsWireMviEffect() {
        val violations = destinationScope.files
            .filter {
                it.path.normalizedPath.endsWith("ScreenRoot.kt") ||
                    it.path.normalizedPath.endsWith("DialogRoot.kt")
            }
            .filter { "state.effect" in it.text && "MviEffect(" !in it.text }
            .map { it.path }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — Effect Handling",
            "$PROFILE_DESTINATION/ProfileScreenRoot.kt",
        )
    }

    @Test
    fun everyNavKeyHasAMatchingEntryRegistration() {
        val navigationFilesByOwner = destinationScope.files
            .filter { "/navigation/" in it.path.normalizedPath }
            .groupBy { navigationOwner(it.path) }
        val violations = navigationFilesByOwner.flatMap { (owner, files) ->
            val entryNames = files
                .flatMap { ENTRY_REGEX.findAll(it.text).map { match -> match.groupValues[1] } }
                .toSet()
            val serializerNames = files
                .flatMap { SUBCLASS_REGEX.findAll(it.text).map { match -> match.groupValues[1] } }
                .toSet()
            files
                .filter { it.path.normalizedPath.endsWith("NavigationRoute.kt") }
                .flatMap { it.objects() }
                .filter { it.hasParentWithName("NavKey") && (it.name !in entryNames || it.name !in serializerNames) }
                .map { "$owner: ${it.location}" }
        }

        assertNoViolations(
            violations,
            ".claude/rules/navigation.md — Adding a New Destination",
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/navigation/ProfileNavigation.kt",
        )
    }

    @Test
    fun consumeEffectBranchesAreCanonical() {
        val violations = onIntentFunctions().mapNotNull { function ->
            val branches = CONSUME_EFFECT_BRANCH_REGEX.findAll(function.text).count()
            val canonicalBranches = CANONICAL_CONSUME_EFFECT_BRANCH_REGEX.findAll(function.text).count()
            function.location.takeUnless { branches > 0 && branches == canonicalBranches }
        }

        assertNoViolations(
            violations,
            ".claude/rules/mvi-architecture.md — Data Flow",
            "$PROFILE_DESTINATION/ProfileViewModel.kt",
        )
    }

    private fun destinationTypes() = destinationScope.classesAndInterfacesAndObjects(
        includeNested = false,
        includeLocal = false,
    )

    private fun destinationViewModelTypes() = destinationTypes()
        .filter { it.name.endsWith("ViewModel") && it.path.normalizedPath.contains("/destination/") }

    private fun destinationViewModels() = destinationViewModelTypes().filterIsInstance<KoClassDeclaration>()

    private fun destinationStateTypes() = destinationTypes()
        .filter {
            it.path.normalizedPath.contains("/destination/") &&
                (it.name.endsWith("State") || it.name.endsWith("ViewModelState"))
        }

    private fun destinationStateClasses() = destinationStateTypes().filterIsInstance<KoClassDeclaration>()

    private fun onIntentFunctions() = destinationViewModels()
        .flatMap { it.functions(includeNested = false, includeLocal = false) }
        .filter { it.name == "onIntent" }

    private fun assertNoViolations(
        violations: List<String>,
        rule: String,
        reference: String,
    ) {
        assertTrue(violations.isEmpty(), failureMessage(rule, reference, violations))
    }

    private fun failureMessage(
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

    private fun KoClassDeclaration.hasParentTypeNamed(name: String): Boolean =
        parents().any { it.name.substringBefore('<') == name }

    private val String.normalizedPath: String
        get() = replace('\\', '/')

    private fun navigationOwner(path: String): String {
        val normalizedPath = path.normalizedPath
        val featureMatch = FEATURE_OWNER_REGEX.find(normalizedPath)
        if (featureMatch != null) return "app/feature/${featureMatch.groupValues[1]}"
        return "template"
    }

    private fun String.countOccurrences(value: String): Int = windowed(value.length).count { it == value }

    companion object {
        private const val PROFILE_DESTINATION =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile"

        private val destinationScope = Konsist
            .scopeFromDirectory("app/feature", "template")
            .slice { COMMON_MAIN_PATH_SEGMENT in it.path.replace('\\', '/') }
        private val coreMviScope = Konsist
            .scopeFromDirectory("app/core/mvi")
            .slice { COMMON_MAIN_PATH_SEGMENT in it.path.replace('\\', '/') }
        private const val COMMON_MAIN_PATH_SEGMENT = "/src/commonMain/"
        private val REQUIRED_MVI_SUFFIXES = setOf("State", "Intent", "Effect", "ViewModelState")
        private val MUTABLE_STATE_TYPE_NAMES = setOf(
            "MutableList",
            "MutableSet",
            "MutableMap",
            "ArrayList",
            "HashMap",
            "HashSet",
            "MutableStateFlow",
            "MutableSharedFlow",
        )
        private val COMPONENT_PATH_REGEX = Regex("/destination/[^/]+/component/")
        private val DESTINATION_VIEW_MODEL_FILE_REGEX = Regex("/destination/[^/]+/[^/]+ViewModel\\.kt$")
        private val INTENT_IMPORT_REGEX = Regex("(?m)^\\s*import\\s+[\\w.]+Intent\\s*$")
        private val INTENT_REFERENCE_REGEX = Regex("\\b\\w+Intent\\b")
        private val ENTRY_REGEX = Regex("entry<(\\w+)>")
        private val SUBCLASS_REGEX = Regex("subclass\\((\\w+)::class")
        private val FEATURE_OWNER_REGEX = Regex("/app/feature/([^/]+)/")
        private val CONSUME_EFFECT_BRANCH_REGEX = Regex("ConsumeEffect\\s*->")
        private val CANONICAL_CONSUME_EFFECT_BRANCH_REGEX = Regex(
            "(?m)^\\s*is\\s+\\w+Intent\\.ConsumeEffect\\s*->\\s*consumeEffect\\(\\)\\s*$",
        )
    }
}
