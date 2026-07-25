package io.github.kei_1111.test.tags

/**
 * E2E テストが DOM から要素を掴むための `Modifier.testTag` 値。
 *
 * CMP は testTag を無加工で DOM の `id` にするため、値は
 * `feature-component-role[-key]` 形式の kebab-case（ASCII 英数字と `-` のみ）で、
 * ドキュメント内で一意にする。feature ごとのネストオブジェクトに定数を置き、
 * リスト要素など key 付きのタグは同じ場所に関数として定義する。
 * Compose 側と Playwright 側はこの定数を共有する。
 */
object TestTags {
    object Profile {
        const val TITLE_BAR_THEME_TOGGLE = "profile-title-bar-theme-toggle"
    }
}
