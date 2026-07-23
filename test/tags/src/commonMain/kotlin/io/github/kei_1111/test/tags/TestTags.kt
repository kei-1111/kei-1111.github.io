package io.github.kei_1111.test.tags

/**
 * E2E テストが DOM から要素を掴むための `Modifier.testTag` 値。
 *
 * CMP は testTag を無加工で DOM の `id` にするため、値は
 * `feature-component-role[-key]` 形式の kebab-case（ASCII 英数字と `-` のみ）で、
 * ドキュメント内で一意にする。Compose 側と Playwright 側はこの定数を共有する。
 */
object TestTags {
    const val TITLE_BAR_THEME_TOGGLE = "title-bar-theme-toggle"
}
