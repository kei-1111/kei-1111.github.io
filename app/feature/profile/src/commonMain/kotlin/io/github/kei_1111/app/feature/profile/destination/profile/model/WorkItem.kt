package io.github.kei_1111.app.feature.profile.destination.profile.model

import io.github.kei_1111.shared.model.LocalizedText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Works プレビューカード / エディタコードで共有する作品データ。[id] は作品を一意に識別する安定キー（将来の作品別 testTag / 選択保持用）。 */
internal data class WorkItem(
    val id: String,
    val name: String,
    val stack: String,
    val description: LocalizedText,
    val tags: ImmutableList<String>,
    /** スクショ URL。読み込み基盤は未導入で、当面は枚数分プレースホルダを描く。 */
    val screenshots: ImmutableList<String>,
    val storeUrl: String?,
    val sourceUrl: String?,
)

/** 作品 API 接続までのプレースホルダ（Issue #24 のフォローアップで実データに置き換える）。 */
internal val TodoWorks: ImmutableList<WorkItem> = persistentListOf(
    WorkItem(
        id = "todo-1",
        name = "TODO",
        stack = "Kotlin · Jetpack Compose",
        description = LocalizedText(ja = "作品情報は準備中です", en = "Work details coming soon"),
        tags = persistentListOf("TODO"),
        screenshots = persistentListOf("todo", "todo", "todo"),
        storeUrl = null,
        sourceUrl = null,
    ),
    WorkItem(
        id = "todo-2",
        name = "TODO",
        stack = "Kotlin · Jetpack Compose",
        description = LocalizedText(ja = "作品情報は準備中です", en = "Work details coming soon"),
        tags = persistentListOf("TODO"),
        screenshots = persistentListOf("todo", "todo"),
        storeUrl = null,
        sourceUrl = null,
    ),
    WorkItem(
        id = "todo-3",
        name = "TODO",
        stack = "Kotlin · Jetpack Compose",
        description = LocalizedText(ja = "作品情報は準備中です", en = "Work details coming soon"),
        tags = persistentListOf("TODO"),
        screenshots = persistentListOf("todo"),
        storeUrl = null,
        sourceUrl = null,
    ),
)
