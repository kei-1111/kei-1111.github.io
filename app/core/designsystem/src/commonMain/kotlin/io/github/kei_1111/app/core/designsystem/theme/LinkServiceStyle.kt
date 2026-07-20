package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.shared.model.LinkServiceType
import kei_1111.app.core.designsystem.generated.resources.Res
import kei_1111.app.core.designsystem.generated.resources.ic_link_github
import kei_1111.app.core.designsystem.generated.resources.ic_link_note
import kei_1111.app.core.designsystem.generated.resources.ic_link_note_light
import kei_1111.app.core.designsystem.generated.resources.ic_link_qiita
import kei_1111.app.core.designsystem.generated.resources.ic_link_x
import org.jetbrains.compose.resources.DrawableResource

/** リンクサービスのアイコン。 */
val LinkServiceType.icon: DrawableResource
    get() = when (this) {
        LinkServiceType.GitHub -> Res.drawable.ic_link_github
        LinkServiceType.X -> Res.drawable.ic_link_x
        LinkServiceType.Qiita -> Res.drawable.ic_link_qiita
        // ダークは note 公式ロゴ（角丸スクエア）。白抜きスクエアのロゴは明るい背景では浮いて見えるため、
        // ライトは "n" グリフのみのシンプル版にする
        LinkServiceType.Note ->
            if (KeiThemeController.isDark) Res.drawable.ic_link_note else Res.drawable.ic_link_note_light
    }

/** リンクサービスのアイコン tint / ホバー枠線色。 */
val LinkServiceType.brandColor: Color
    get() = when (this) {
        LinkServiceType.GitHub, LinkServiceType.X, LinkServiceType.Note -> keiColorScheme.textPrimary
        LinkServiceType.Qiita -> keiColorScheme.brandQiita
    }
