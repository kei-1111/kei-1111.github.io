package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import io.github.kei_1111.shared.model.LinkServiceType
import kei_1111.app.core.designsystem.generated.resources.Res
import kei_1111.app.core.designsystem.generated.resources.ic_link_github
import kei_1111.app.core.designsystem.generated.resources.ic_link_note
import kei_1111.app.core.designsystem.generated.resources.ic_link_qiita
import kei_1111.app.core.designsystem.generated.resources.ic_link_x
import org.jetbrains.compose.resources.DrawableResource

// note だけ明暗で別ロゴを使う（ダークは公式の角丸スクエア、ライトは "n" グリフのみ）。
// 出し分けは drawable / drawable-dark の修飾子でリソース側が解決する。
fun LinkServiceType.icon(): DrawableResource = when (this) {
    LinkServiceType.GitHub -> Res.drawable.ic_link_github
    LinkServiceType.X -> Res.drawable.ic_link_x
    LinkServiceType.Qiita -> Res.drawable.ic_link_qiita
    LinkServiceType.Note -> Res.drawable.ic_link_note
}

fun LinkServiceType.brandColor(colors: KeiColorScheme): Color = when (this) {
    LinkServiceType.GitHub, LinkServiceType.X, LinkServiceType.Note -> colors.textPrimary
    LinkServiceType.Qiita -> colors.brandQiita
}
