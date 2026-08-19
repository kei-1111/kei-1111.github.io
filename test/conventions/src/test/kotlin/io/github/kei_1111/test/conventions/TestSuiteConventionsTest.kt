package io.github.kei_1111.test.conventions

import com.lemonappdev.konsist.api.KoModifier
import org.junit.jupiter.api.Test

class TestSuiteConventionsTest {

    @Test
    fun testAnnotationsComeFromTheRightFramework() {
        val nonE2eViolations = nonE2eTestFiles()
            .filter { TEST_ANNOTATION_REGEX.containsMatchIn(it.text) }
            .mapNotNull { file ->
                val imports = file.imports.map { it.name }.toSet()
                file.path.takeUnless {
                    KOTLIN_TEST_IMPORT in imports && JUNIT_TEST_IMPORT !in imports
                }
            }
        val e2eViolations = e2eScope.files
            .filter { TEST_ANNOTATION_REGEX.containsMatchIn(it.text) }
            .mapNotNull { file ->
                val imports = file.imports.map { it.name }.toSet()
                file.path.takeUnless {
                    JUNIT_TEST_IMPORT in imports && KOTLIN_TEST_IMPORT !in imports
                }
            }
        val mixedImportViolations = (nonE2eTestFiles() + e2eScope.files)
            .distinctBy { it.path.normalizedPath }
            .mapNotNull { file ->
                val imports = file.imports.map { it.name }.toSet()
                file.path.takeIf { KOTLIN_TEST_IMPORT in imports && JUNIT_TEST_IMPORT in imports }
            }

        assertNoViolations(
            (nonE2eViolations + e2eViolations + mixedImportViolations).distinct(),
            TEST_FRAMEWORK_RULES,
            USE_CASE_TEST_REFERENCE,
        )
    }

    @Test
    fun testClassesShareTheirSubjectsPackage() {
        val subjectPackages = productionTypes()
            .groupBy(
                keySelector = { it.name },
                valueTransform = { it.packagee?.name },
            )
            .mapValues { (_, packages) -> packages.filterNotNull().toSet() }
        val violations = nonE2eTestClasses()
            .filter { it.name.endsWith("Test") }
            .mapNotNull { testClass ->
                val subjectName = testClass.name.removeSuffix("Test")
                val packages = subjectPackages[subjectName] ?: return@mapNotNull null
                val packageName = testClass.packagee?.name
                testClass.location.takeUnless { packageName != null && packageName in packages }
            }

        assertNoViolations(violations, NON_E2E_TEST_RULES, USE_CASE_TEST_REFERENCE)
    }

    @Test
    fun fakesAreLocalPrivateOrPromotedIntoFakePackages() {
        val violations = allTestClasses()
            .filter { it.name.startsWith("Fake") }
            .mapNotNull { fake ->
                val path = fake.path.normalizedPath
                val promotedFakeIsValid = fake.hasInternalModifier &&
                    ("/app/" !in path || "/fake/" in path)
                fake.location.takeUnless { fake.hasPrivateModifier || promotedFakeIsValid }
            }

        assertNoViolations(violations, NON_E2E_TEST_RULES, USE_CASE_TEST_REFERENCE)
    }

    @Test
    fun e2eTestsAreFlatAndExtendPlaywrightTestBase() {
        val packageViolations = e2eScope.files
            .filter { it.packagee != null }
            .mapNotNull { file ->
                val packageName = file.packagee?.name
                file.path.takeUnless { packageName != null && packageName in E2E_PACKAGES }
            }
        val testClassViolations = e2eScope.classes(includeNested = false, includeLocal = false)
            .filter { testClass ->
                testClass.functions(includeNested = false, includeLocal = false)
                    .any { it.hasAnnotationWithName("Test") }
            }
            .mapNotNull { testClass ->
                val valid = testClass.name.endsWith("E2eTest") &&
                    testClass.hasParentTypeNamed("PlaywrightTestBase") &&
                    testClass.packagee?.name == E2E_ROOT_PACKAGE
                testClass.location.takeUnless { valid }
            }

        assertNoViolations(
            packageViolations + testClassViolations,
            E2E_TEST_RULE,
            E2E_TEST_REFERENCE,
        )
    }

    @Test
    fun apiFixturesAreInternalObjectsWithFulfill() {
        val violations = e2eScope.objects(includeNested = false)
            .filter { it.name.endsWith("ApiFixture") }
            .mapNotNull { fixture ->
                val hasFulfill = fixture.functions(includeNested = false, includeLocal = false)
                    .any { function ->
                        function.name == "fulfill" &&
                            function.parameters.firstOrNull()?.type?.name?.substringBefore('<') == "Page"
                    }
                fixture.location.takeUnless { fixture.hasInternalModifier && hasFulfill }
            }

        assertNoViolations(violations, E2E_TEST_RULE, E2E_TEST_REFERENCE)
    }

    @Test
    fun pageObjectsWrapThePageAndCarryNoTests() {
        val violations = e2eScope.classes(includeNested = false, includeLocal = false)
            .filter { it.packagee?.name == E2E_PAGE_PACKAGE }
            .mapNotNull { pageObject ->
                val parameters = pageObject.primaryConstructor?.parameters.orEmpty()
                val page = parameters.singleOrNull()
                val hasTestFunction = pageObject.functions(includeNested = true, includeLocal = false)
                    .any { it.hasAnnotationWithName("Test") }
                val valid = page != null &&
                    page.hasModifier(KoModifier.PRIVATE) &&
                    page.hasValModifier &&
                    page.name == "page" &&
                    page.type.name.substringBefore('<') == "Page" &&
                    !hasTestFunction
                pageObject.location.takeUnless { valid }
            }

        assertNoViolations(violations, E2E_TEST_RULE, E2E_TEST_REFERENCE)
    }

    private fun nonE2eTestFiles() = (
        clientTestScope.files +
            sharedModelTestScope.files +
            serverTestScope.files
        ).distinctBy { it.path.normalizedPath }

    private fun nonE2eTestClasses() = (
        clientTestScope.classes(includeNested = false, includeLocal = false) +
            sharedModelTestScope.classes(includeNested = false, includeLocal = false) +
            serverTestScope.classes(includeNested = false, includeLocal = false)
        ).distinctBy { it.location }

    private fun allTestClasses() = (
        nonE2eTestClasses() +
            e2eScope.classes(includeNested = false, includeLocal = false)
        ).distinctBy { it.location }

    private fun productionTypes() = (
        appScope
            .slice { COMMON_MAIN_PATH_SEGMENT in it.path.normalizedPath }
            .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false) +
            sharedModelScope.classesAndInterfacesAndObjects(includeNested = false, includeLocal = false) +
            serverScope.classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
        ).distinctBy { it.location }

    companion object {
        private const val TEST_FRAMEWORK_RULES =
            ".claude/rules/app-testing.md; .claude/rules/server-testing.md; .claude/rules/ui-testing.md"
        private const val NON_E2E_TEST_RULES =
            ".claude/rules/app-testing.md; .claude/rules/server-testing.md"
        private const val E2E_TEST_RULE = ".claude/rules/ui-testing.md"
        private const val USE_CASE_TEST_REFERENCE =
            "app/core/domain/src/commonTest/kotlin/io/github/kei_1111/app/core/domain/usecase/GetProfileUseCaseTest.kt"
        private const val E2E_TEST_REFERENCE =
            "test/e2e/src/test/kotlin/io/github/kei_1111/test/e2e/ThemeToggleE2eTest.kt"
        private const val KOTLIN_TEST_IMPORT = "kotlin.test.Test"
        private const val JUNIT_TEST_IMPORT = "org.junit.jupiter.api.Test"
        private const val E2E_ROOT_PACKAGE = "io.github.kei_1111.test.e2e"
        private const val E2E_PAGE_PACKAGE = "$E2E_ROOT_PACKAGE.page"
        private val E2E_PACKAGES = setOf(E2E_ROOT_PACKAGE, E2E_PAGE_PACKAGE)
        private val TEST_ANNOTATION_REGEX = Regex("@Test\\b")
    }
}
