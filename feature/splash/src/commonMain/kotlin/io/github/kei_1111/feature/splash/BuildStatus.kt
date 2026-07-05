package io.github.kei_1111.feature.splash

/** スプラッシュ全体のビルド状態。Failed になったら Profile へは遷移しない */
internal enum class BuildStatus {
    Running,
    Success,
    Failed,
}
