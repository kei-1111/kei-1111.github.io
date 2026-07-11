package io.github.kei_1111.core.designsystem.theme

import kei_1111.core.designsystem.generated.resources.Res
import kei_1111.core.designsystem.generated.resources.img_profile_icon
import org.jetbrains.compose.resources.DrawableResource

/**
 * プロフィールアイコン画像。
 *
 * feature:profile（TitleBar / GitHubPreviewCard）と feature:splash（Splash アイコン）の
 * 双方から同一アセットを参照するため、composeResources の Res クラスをモジュールごとに
 * 公開する（compose.resources { publicResClass = true }）代わりに、
 * このプロパティ1つだけを designsystem の公開面として絞って提供する。
 */
val ProfileIconImage: DrawableResource
    get() = Res.drawable.img_profile_icon
