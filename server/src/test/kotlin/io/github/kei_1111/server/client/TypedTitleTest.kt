package io.github.kei_1111.server.client

import kotlin.test.Test
import kotlin.test.assertEquals

class TypedTitleTest {

    @Test
    fun parsesAConventionalCommitsTitle() {
        assertEquals(TypedTitle(type = "chore", title = "some task"), parseTypedTitle("chore: some task"))
    }

    @Test
    fun parsesALegacyBracketedTitle() {
        assertEquals(TypedTitle(type = "Bug", title = "crash on load"), parseTypedTitle("[Bug]: crash on load"))
    }

    @Test
    fun parsesAConventionalCommitsTitleWithABreakingChangeMarker() {
        assertEquals(TypedTitle(type = "feat!", title = "drop legacy parsing"), parseTypedTitle("feat!: drop legacy parsing"))
    }

    @Test
    fun parsesAConventionalCommitsTitleWithAScopeAndABreakingChangeMarker() {
        assertEquals(TypedTitle(type = "fix(server)!", title = "drop it"), parseTypedTitle("fix(server)!: drop it"))
    }

    @Test
    fun parsesAConventionalCommitsTitleWithAScope() {
        assertEquals(
            TypedTitle(type = "fix(server)", title = "parse Conventional Commits Issue and PR titles"),
            parseTypedTitle("fix(server): parse Conventional Commits Issue and PR titles"),
        )
    }

    @Test
    fun keepsATitleWithoutATypePrefixUntouched() {
        assertEquals(
            TypedTitle(type = null, title = "作品ページの追加（作品 API + クライアント UI）"),
            parseTypedTitle("作品ページの追加（作品 API + クライアント UI）"),
        )
    }

    @Test
    fun keepsANonConventionalColonTitleUntouched() {
        assertEquals(TypedTitle(type = null, title = "Note: keep as-is"), parseTypedTitle("Note: keep as-is"))
    }
}
