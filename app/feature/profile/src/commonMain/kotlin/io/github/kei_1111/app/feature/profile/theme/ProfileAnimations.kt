@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.theme

internal data object ProfileAnimations {
    /** 点滅キャレットの1周期（step-end 相当） */
    const val CaretBlinkMillis = 1100

    /** ホバー時の色フェードの遷移時間 */
    const val HoverTransitionMillis = 120

    /** ローディングパルス片道の時間（RepeatMode.Reverse で1往復 2400ms） */
    const val ContributionsPulseMillis = 1200

    /** ライセンスシートのスライド + フェードの遷移時間 */
    const val SheetTransitionMillis = 300
}
