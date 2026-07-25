package io.github.kei_1111.app.feature.splash.destination.splash.model

/** スプラッシュ全体のビルド状態。Failed になったら Profile へは遷移しない */
internal enum class BuildStatus {
    Running,
    Success,
    Failed,
}
