@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.theme

internal data object ProfileAnimations {
    /** step-end 相当 */
    const val CaretBlinkMillis = 1100

    /** RepeatMode.Reverse で1往復 2400ms */
    const val ContributionsPulseMillis = 1200

    const val SheetTransitionMillis = 300

    /** linear, infinite */
    const val ShimmerCycleMillis = 1600

    /** linear, infinite */
    const val PreviewSpinnerCycleMillis = 900

    /** linear, infinite */
    const val PreviewBuildingBarCycleMillis = 1400

    const val ContentCrossfadeMillis = 200
}
