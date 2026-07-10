package io.github.kei_1111.feature.profile.destination.profile.component.githubcard

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.core.designsystem.theme.KeiThemeController
import io.github.kei_1111.core.designsystem.theme.keiColorScheme
import io.github.kei_1111.core.model.LinkServiceType
import kei_1111.feature.profile.generated.resources.Res
import kei_1111.feature.profile.generated.resources.ic_link_github
import kei_1111.feature.profile.generated.resources.ic_link_note
import kei_1111.feature.profile.generated.resources.ic_link_note_light
import kei_1111.feature.profile.generated.resources.ic_link_qiita
import kei_1111.feature.profile.generated.resources.ic_link_x
import org.jetbrains.compose.resources.DrawableResource

/** LINKS タイルのアイコン。実アイコンリソースは旧 GitHubProfileContent と同じもの。 */
internal val LinkServiceType.icon: DrawableResource
    get() = when (this) {
        LinkServiceType.GitHub -> Res.drawable.ic_link_github
        LinkServiceType.X -> Res.drawable.ic_link_x
        LinkServiceType.Qiita -> Res.drawable.ic_link_qiita
        // ダークは note 公式ロゴ（角丸スクエア）。白抜きスクエアのロゴは明るい背景では浮いて見えるため、
        // ライトは "n" グリフのみのシンプル版にする
        LinkServiceType.Note ->
            if (KeiThemeController.isDark) Res.drawable.ic_link_note else Res.drawable.ic_link_note_light
    }

/** LINKS タイルのアイコン tint / ホバー枠線色。実際のブランドカラーは旧 GitHubProfileContent と同じ値。 */
internal val LinkServiceType.brandColor: Color
    get() = when (this) {
        LinkServiceType.GitHub, LinkServiceType.X, LinkServiceType.Note -> keiColorScheme.textPrimary
        LinkServiceType.Qiita -> keiColorScheme.brandQiita
    }
