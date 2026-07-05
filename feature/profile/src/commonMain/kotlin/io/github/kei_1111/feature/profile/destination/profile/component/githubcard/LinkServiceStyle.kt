package io.github.kei_1111.feature.profile.destination.profile.component.githubcard

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.core.designsystem.theme.keiColorScheme
import io.github.kei_1111.core.model.LinkServiceType
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_link_github
import kei_1111.feature.profile.generated.resources.ic_link_qiita
import kei_1111.feature.profile.generated.resources.ic_link_x
import kei_1111.feature.profile.generated.resources.ic_link_zenn
import org.jetbrains.compose.resources.DrawableResource

/** LINKS タイルのアイコン。実アイコンリソースは旧 GitHubProfileContent と同じもの。 */
internal val LinkServiceType.icon: DrawableResource
    get() = when (this) {
        LinkServiceType.GitHub -> Res.drawable.ic_link_github
        LinkServiceType.X -> Res.drawable.ic_link_x
        LinkServiceType.Qiita -> Res.drawable.ic_link_qiita
        LinkServiceType.Zenn -> Res.drawable.ic_link_zenn
    }

/** LINKS タイルのアイコン tint / ホバー枠線色。実 AS ブランドカラーは旧 GitHubProfileContent と同じ値。 */
internal val LinkServiceType.brandColor: Color
    get() = when (this) {
        LinkServiceType.GitHub, LinkServiceType.X -> keiColorScheme.textPrimary
        LinkServiceType.Qiita -> keiColorScheme.brandQiita
        LinkServiceType.Zenn -> keiColorScheme.brandZenn
    }
