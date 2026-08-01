package io.github.kei_1111.app.feature.splash.destination.splash.model

/** Failed になったら Profile へは遷移しない */
internal enum class BuildStatus {
    Running,
    Success,
    Failed,
}
