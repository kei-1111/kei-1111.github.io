package io.github.kei_1111.test.conventions

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import org.junit.jupiter.api.Test

class PackageConventionsTest {

    @Test
    fun packagesFollowTheModuleKindTable() {
        val violations = templatePackageViolations() +
            featurePackageViolations() +
            corePackageViolations() +
            webAppPackageViolations() +
            sharedModelPackageViolations() +
            serverPackageViolations() +
            testPackageViolations()

        assertNoViolations(
            violations,
            ".claude/rules/naming-conventions.md — Packages",
            PROFILE_PACKAGE_REFERENCE,
        )
    }

    @Test
    fun ioDispatcherNeverAppears() {
        val sharedAndServerScope = Konsist.scopeFromDirectory("shared/model", "server")
        val violations = (appScope.files + destinationScope.files + sharedAndServerScope.files)
            .distinctBy { it.path.normalizedPath }
            .mapNotNull { file -> file.path.takeIf { "IoDispatcher" in file.text } }

        assertNoViolations(
            violations,
            ".claude/rules/data-layer.md",
            "app/core/common/src/commonMain/kotlin/io/github/kei_1111/app/core/common/dispatcher/DispatcherBindings.kt",
        )
    }

    private fun templatePackageViolations() = mainSourceFiles("template").packageViolations { _, packageName ->
        packageName == TEMPLATE_NAVIGATION_PACKAGE ||
            packageName.matchesPackageRoot(TEMPLATE_SCREEN_PACKAGE) ||
            packageName.matchesPackageRoot(TEMPLATE_DIALOG_PACKAGE)
    }

    private fun featurePackageViolations() = mainSourceFiles("app/feature").packageViolations { file, packageName ->
        val moduleName = FEATURE_MODULE_REGEX.find(file.path.normalizedPath)?.groupValues?.get(1)
        if (moduleName == null) {
            false
        } else {
            val prefix = "io.github.kei_1111.app.feature.$moduleName."
            val nextSegment = packageName.removePrefix(prefix).substringBefore('.')
            packageName.startsWith(prefix) && nextSegment in FEATURE_PACKAGE_SEGMENTS
        }
    }

    private fun corePackageViolations() = mainSourceFiles("app/core").packageViolations { file, packageName ->
        val moduleName = CORE_MODULE_REGEX.find(file.path.normalizedPath)?.groupValues?.get(1)
        moduleName != null && packageName.matchesPackageRoot("io.github.kei_1111.app.core.$moduleName")
    }

    private fun webAppPackageViolations() = mainSourceFiles("app/webApp").packageViolations { _, packageName ->
        (packageName == APP_PACKAGE || packageName.startsWith("$APP_PACKAGE.")) &&
            !packageName.startsWith("$APP_PACKAGE.feature.") &&
            !packageName.startsWith("$APP_PACKAGE.core.")
    }

    private fun sharedModelPackageViolations() = mainSourceFiles("shared/model").packageViolations { _, packageName ->
        packageName.matchesPackageRoot("io.github.kei_1111.shared.model")
    }

    private fun serverPackageViolations() = mainSourceFiles("server").packageViolations { _, packageName ->
        packageName.matchesPackageRoot(SERVER_PACKAGE)
    }

    private fun testPackageViolations() = Konsist.scopeFromDirectory("test").files.packageViolations { file, packageName ->
        val moduleName = TEST_MODULE_REGEX.find(file.path.normalizedPath)?.groupValues?.get(1)
        moduleName != null && packageName.matchesPackageRoot("io.github.kei_1111.test.$moduleName")
    }

    private fun mainSourceFiles(directory: String) = Konsist.scopeFromDirectory(directory).files.filter {
        val path = it.path.normalizedPath
        COMMON_MAIN_PATH_SEGMENT in path || MAIN_PATH_SEGMENT in path
    }

    private fun List<KoFileDeclaration>.packageViolations(
        isValid: (KoFileDeclaration, String) -> Boolean,
    ): List<String> = mapNotNull { file ->
        val packageName = file.packagee?.name
        file.path.takeUnless { packageName != null && isValid(file, packageName) }
    }

    private fun String.matchesPackageRoot(root: String): Boolean = this == root || startsWith("$root.")

    companion object {
        private const val PROFILE_PACKAGE_REFERENCE =
            "app/feature/profile/src/commonMain/kotlin/io/github/kei_1111/app/feature/profile/destination/profile"
        private const val MAIN_PATH_SEGMENT = "/src/main/"
        private const val TEMPLATE_NAVIGATION_PACKAGE = "io.github.kei_1111.template.navigation"
        private const val TEMPLATE_SCREEN_PACKAGE = "io.github.kei_1111.template.destination.screen"
        private const val TEMPLATE_DIALOG_PACKAGE = "io.github.kei_1111.template.destination.dialog"
        private const val APP_PACKAGE = "io.github.kei_1111.app"
        private const val SERVER_PACKAGE = "io.github.kei_1111.server"
        private val FEATURE_PACKAGE_SEGMENTS = setOf("destination", "navigation", "model")
        private val FEATURE_MODULE_REGEX = Regex("/app/feature/([^/]+)/")
        private val CORE_MODULE_REGEX = Regex("/app/core/([^/]+)/")
        private val TEST_MODULE_REGEX = Regex("/test/([^/]+)/")
    }
}
