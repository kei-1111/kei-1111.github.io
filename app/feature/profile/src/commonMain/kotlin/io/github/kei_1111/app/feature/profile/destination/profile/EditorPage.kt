package io.github.kei_1111.app.feature.profile.destination.profile

/** エディタ右上の表示モード切替（実 AS の Code / Split / Design に相当）。 */
internal enum class EditorViewMode {
    CodeOnly,
    Split,
    PreviewOnly,
}

/** エディタのタブ / ツリー選択 / ステータスバーのパンくずが同期して切り替わる対象。 */
internal enum class EditorPage(
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
