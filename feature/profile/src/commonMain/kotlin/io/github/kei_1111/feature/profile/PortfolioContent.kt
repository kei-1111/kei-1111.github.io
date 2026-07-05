package io.github.kei_1111.feature.profile

import io.github.kei_1111.core.data.BasicInfoSet
import org.jetbrains.compose.resources.DrawableResource

/**
 * IDE 風ポートフォリオに表示するコンテンツ。
 */
data object PortfolioContent {
    // eager にすると object 初期化時に core:data の生成リソースをロードしてしまい、
    // 文字列しか使わない Preview (CodeContent 等) まで巻き込んで失敗するため lazy にする
    val profileIcon: DrawableResource by lazy { BasicInfoSet.profileIcon }
}

/** エディタ右上の表示モード切替（実 AS の Code / Split / Design に相当）。 */
enum class EditorViewMode {
    CodeOnly,
    Split,
    PreviewOnly,
}

/** エディタのタブ / ツリー選択 / ステータスバーのパンくずが同期して切り替わる対象。 */
enum class EditorPage(
    val fileName: String,
    val breadcrumb: String,
    val previewName: String,
) {
    Profile(
        fileName = "ProfileScreen.kt",
        breadcrumb = "app › src › main › kotlin › io.github.kei_1111 › ui.profile › ProfileScreen.kt",
        previewName = "ProfileScreenPreview",
    ),
}
