package io.github.kei_1111.test.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.github.kei_1111.test.e2e.page.ProfilePage
import org.junit.jupiter.api.Test

/**
 * Issue 一覧の中身はライブデータ（API 到達性に依存）なので
 * アサートせず、クロームの挙動だけを検証する（ui-testing.md — Scope）。
 */
class TodoToolWindowE2eTest : PlaywrightTestBase() {

    @Test
    fun togglingTodoToolWindowShowsAndHidesThePanel() {
        val profile = ProfilePage(page)

        assertThat(profile.todoPanel()).hasCount(0)

        profile.toggleTodoRail()
        assertThat(profile.todoPanel()).isVisible()

        profile.toggleTodoRail()
        assertThat(profile.todoPanel()).hasCount(0)
    }
}
