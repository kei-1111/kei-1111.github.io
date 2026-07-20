package io.github.kei_1111.app.feature.profile.destination.profile.component.githubcard

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.app.core.designsystem.theme.KeiColorScheme
import io.github.kei_1111.shared.model.LinkServiceType
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.ic_link_github
import kei_1111.app.feature.profile.generated.resources.ic_link_note
import kei_1111.app.feature.profile.generated.resources.ic_link_note_light
import kei_1111.app.feature.profile.generated.resources.ic_link_qiita
import kei_1111.app.feature.profile.generated.resources.ic_link_x
import org.jetbrains.compose.resources.DrawableResource

/** LINKS タイルのアイコン。実アイコンリソースは旧 GitHubProfileContent と同じもの。 */
internal fun LinkServiceType.icon(colors: KeiColorScheme): DrawableResource = when (this) {
    LinkServiceType.GitHub -> Res.drawable.ic_link_github
    LinkServiceType.X -> Res.drawable.ic_link_x
    LinkServiceType.Qiita -> Res.drawable.ic_link_qiita
    // ダークは note 公式ロゴ（角丸スクエア）。白抜きスクエアのロゴは明るい背景では浮いて見えるため、
    // ライトは "n" グリフのみのシンプル版にする
    LinkServiceType.Note ->
        if (colors.isDark) Res.drawable.ic_link_note else Res.drawable.ic_link_note_light
}

/** LINKS タイルのアイコン tint / ホバー枠線色。実際のブランドカラーは旧 GitHubProfileContent と同じ値。 */
internal fun LinkServiceType.brandColor(colors: KeiColorScheme): Color = when (this) {
    LinkServiceType.GitHub, LinkServiceType.X, LinkServiceType.Note -> colors.textPrimary
    LinkServiceType.Qiita -> colors.brandQiita
}
