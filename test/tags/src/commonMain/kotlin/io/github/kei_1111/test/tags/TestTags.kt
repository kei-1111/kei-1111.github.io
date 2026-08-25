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
        const val TITLE_BAR_LANGUAGE_TOGGLE = "profile-title-bar-language-toggle"
        const val TITLE_BAR_SEARCH = "profile-title-bar-search"
        const val TOOL_RAIL_PROJECT = "profile-tool-rail-project"
        const val TOOL_RAIL_LOGCAT = "profile-tool-rail-logcat"
        const val TOOL_RAIL_TODO = "profile-tool-rail-todo"
        const val TOOL_RAIL_TERMINAL = "profile-tool-rail-terminal"
        const val EDITOR_INPUT = "profile-editor-input"
        const val README_PREVIEW = "profile-readme-preview"
        const val GITHUB_CARD = "profile-github-card"
        const val LOGCAT_ENTRIES = "profile-logcat-entries"
        const val BOTTOM_PANEL_DRAG_HANDLE = "profile-bottom-panel-drag-handle"
        const val TOOL_RAIL_CHANGELOG = "profile-tool-rail-changelog"
        const val TERMINAL_INPUT = "profile-terminal-input"
        const val TERMINAL_HIDE = "profile-terminal-hide"
        const val TERMINAL_TAB_CLOSE = "profile-terminal-tab-close"
        const val LOGCAT_CLEAR = "profile-logcat-clear"
        const val LOGCAT_HIDE = "profile-logcat-hide"
        const val LOGCAT_TAB_CLOSE = "profile-logcat-tab-close"
        const val TODO_PANEL = "profile-todo-panel"
        const val CHANGELOG_PANEL = "profile-changelog-panel"
        const val CHANGELOG_HIDE = "profile-changelog-hide"
        const val CHANGELOG_RETRY = "profile-changelog-retry"
        const val EDITOR_USAGE_PAGE = "profile-editor-usage-page"
        const val VIEW_MODE_CODE = "profile-view-mode-code"
        const val VIEW_MODE_SPLIT = "profile-view-mode-split"
        const val VIEW_MODE_PREVIEW = "profile-view-mode-preview"
        const val PREVIEW_RETRY = "profile-preview-retry"
        const val LICENSE_SHEET_CLOSE = "profile-license-sheet-close"
        const val LICENSE_SHEET_CLOSE_FOOTER = "profile-license-sheet-close-footer"
        const val LICENSE_SHEET_SCRIM = "profile-license-sheet-scrim"
        const val WORKS_PREV = "profile-works-prev"
        const val WORKS_NEXT = "profile-works-next"
        const val WORKS_POSITION = "profile-works-position"
        const val WORKS_DETAIL = "profile-works-detail"
        const val WORKS_SHEET_CLOSE = "profile-works-sheet-close"
        const val WORKS_SHEET_SCRIM = "profile-works-sheet-scrim"
        const val NOTIFICATION_BALLOON_SITE_UPDATED = "profile-notification-balloon-site-updated"
        const val NOTIFICATION_BALLOON_SITE_UPDATED_ACTION = "profile-notification-balloon-site-updated-action"
        const val NOTIFICATION_BALLOON_FALLBACK_WARNING = "profile-notification-balloon-fallback-warning"

        fun projectTreeItem(key: String) = "profile-project-tree-item-$key"

        fun editorTab(key: String) = "profile-editor-tab-$key"

        fun editorTabClose(key: String) = "profile-editor-tab-close-$key"

        fun licenseRow(key: String) = "profile-license-row-$key"

        fun changelogRow(key: String) = "profile-changelog-row-$key"
    }

    object SearchEverywhere {
        const val FIELD = "search-everywhere-field"

        fun tab(key: String) = "search-everywhere-tab-$key"

        fun result(key: String) = "search-everywhere-result-$key"
    }
}
