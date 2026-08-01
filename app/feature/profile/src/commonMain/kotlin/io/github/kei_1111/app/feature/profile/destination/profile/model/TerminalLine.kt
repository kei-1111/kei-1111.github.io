package io.github.kei_1111.app.feature.profile.destination.profile.model

/** IDE チュローム扱いで言語によらず固定（`naming-conventions.md` — Text Content）。 */
internal const val TERMINAL_PROMPT = "kei@keis-macbook-pro kei-1111.github.io %"

/** 超過分は古い行から捨てる（InteractionLog の MAX_ENTRIES と同趣旨）。 */
internal const val TERMINAL_MAX_LINES = 400

/** [kind] は行の配色分け（コマンドエコー / 通常出力 / エラー / 成功）に使う。 */
internal data class TerminalLine(
    val text: String,
    val kind: TerminalLineKind,
)

internal enum class TerminalLineKind {
    /** ユーザーが打ったコマンドのエコー行（プロンプト付き）。 */
    Command,
    Output,
    Error,

    /** `BUILD SUCCESSFUL` などの強調行。 */
    Success,
}
