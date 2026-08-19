package io.github.kei_1111.test.conventions

import com.lemonappdev.konsist.api.declaration.KoImportDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.KoPackageDeclaration
import org.junit.jupiter.api.Test

class UseCaseConventionsTest {

    @Test
    fun useCaseFilesPairPublicInterfaceWithInternalImpl() {
        val violations = useCaseFiles().mapNotNull { file ->
            val interfaces = file.interfaces(includeNested = false)
            val classes = file.classes(includeNested = false, includeLocal = false)
            val useCase = interfaces.singleOrNull()
            val invoke = useCase
                ?.functions(includeNested = false, includeLocal = false)
                ?.singleOrNull()
            val implementation = classes.singleOrNull()
            val topLevelDeclarations = file.declarations(includeNested = false, includeLocal = false)
                .filterNot { it is KoPackageDeclaration || it is KoImportDeclaration }
            val valid = topLevelDeclarations.size == 2 &&
                useCase != null &&
                useCase.hasPublicOrDefaultModifier &&
                useCase.name.endsWith("UseCase") &&
                invoke?.name == "invoke" &&
                invoke?.hasOperatorModifier == true &&
                implementation != null &&
                implementation.hasInternalModifier &&
                implementation.name == "${useCase.name}Impl" &&
                implementation.hasParentTypeNamed(useCase.name)
            file.path.takeUnless { valid }
        }

        assertNoViolations(violations, USE_CASE_RULE, USE_CASE_REFERENCE)
    }

    @Test
    fun useCaseImplsDependOnExactlyOneRepository() {
        val violations = useCaseImpls().mapNotNull { implementation ->
            val parameterTypeNames = implementation.primaryConstructor
                ?.parameters
                .orEmpty()
                .mapNotNull { it.type?.name?.substringBefore('<') }
            val valid = parameterTypeNames.count { it.endsWith("Repository") } == 1 &&
                parameterTypeNames.none { it.endsWith("UseCase") }
            implementation.location.takeUnless { valid }
        }

        assertNoViolations(violations, USE_CASE_RULE, USE_CASE_REFERENCE)
    }

    @Test
    fun getUseCasesReturnFlowAndSaveUseCasesSuspendUnit() {
        val violations = useCaseInterfaces().mapNotNull { useCase ->
            val invoke = useCase
                .functions(includeNested = false, includeLocal = false)
                .singleOrNull { it.name == "invoke" }
            val returnTypeName = invoke?.returnType?.name?.substringBefore('<')
            val valid = when {
                useCase.name.startsWith("Get") ->
                    returnTypeName == "Flow" && invoke?.hasSuspendModifier != true

                useCase.name.startsWith("Save") ->
                    invoke?.hasSuspendModifier == true && (returnTypeName == null || returnTypeName == "Unit")

                else -> true
            }
            useCase.location.takeUnless { valid }
        }

        assertNoViolations(
            violations,
            "$USE_CASE_RULE; .claude/rules/naming-conventions.md — UseCase",
            USE_CASE_REFERENCE,
        )
    }

    @Test
    fun getUseCaseBodiesApplyDistinctUntilChanged() {
        val violations = useCaseImpls()
            .filter { it.name.startsWith("Get") }
            .mapNotNull { implementation ->
                implementation.location.takeUnless { ".distinctUntilChanged()" in implementation.text }
            }

        assertNoViolations(violations, USE_CASE_RULE, USE_CASE_REFERENCE)
    }

    private fun useCaseFiles() = domainScope.files.filter {
        val path = it.path.normalizedPath
        "/domain/usecase/" in path && path.endsWith("UseCase.kt")
    }

    private fun useCaseInterfaces(): List<KoInterfaceDeclaration> = useCaseFiles()
        .flatMap { it.interfaces(includeNested = false) }
        .filter { it.name.endsWith("UseCase") }

    private fun useCaseImpls() = useCaseFiles()
        .flatMap { it.classes(includeNested = false, includeLocal = false) }
        .filter { it.name.endsWith("UseCaseImpl") }

    companion object {
        private const val USE_CASE_RULE = ".claude/rules/usecase.md"
        private const val USE_CASE_REFERENCE =
            "app/core/domain/src/commonMain/kotlin/io/github/kei_1111/app/core/domain/usecase/GetProfileUseCase.kt"
    }
}
