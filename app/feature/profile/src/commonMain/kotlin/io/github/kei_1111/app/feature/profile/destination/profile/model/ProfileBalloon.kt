package io.github.kei_1111.app.feature.profile.destination.profile.model

/** 右下に出すバルーン通知。[id] は通知種別ごとに固定で、同じ種別が二重に積まれることはない。 */
internal sealed interface ProfileBalloon {
    val id: String

    /** 前回訪問以降にマージされた PR がある。[newPullRequestCount] が null なら初回訪問（件数を出さない）。 */
    data class SiteUpdated(val newPullRequestCount: Int?) : ProfileBalloon {
        override val id: String = ID

        companion object {
            const val ID: String = "site-updated"
        }
    }

    /** GitHub 取得に失敗し、サーバーが静的コンテンツを配信している。 */
    data object FallbackWarning : ProfileBalloon {
        const val ID: String = "fallback-warning"

        override val id: String = ID
    }
}
