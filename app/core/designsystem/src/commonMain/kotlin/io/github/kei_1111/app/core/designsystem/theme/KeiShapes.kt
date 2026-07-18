@file:Suppress("MagicNumber")

package io.github.kei_1111.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** IDE レイアウト共通の形状トークン。 */
@Immutable
data class KeiShapes(
    val island: Shape,
    val pill: Shape,
    val chip: Shape,
    val row: Shape,
    val card: Shape,
    val badge: Shape,

    // GitHub プロフィールカード
    val githubCard: Shape,
    val githubItem: Shape,
    val linkTile: Shape,
)

val keiShapes = KeiShapes(
    island = RoundedCornerShape(12.dp),
    pill = RoundedCornerShape(7.dp),
    chip = RoundedCornerShape(4.dp),
    row = RoundedCornerShape(7.dp),
    card = RoundedCornerShape(10.dp),
    badge = RoundedCornerShape(3.dp),

    githubCard = RoundedCornerShape(14.dp),
    githubItem = RoundedCornerShape(8.dp),
    linkTile = RoundedCornerShape(10.dp),
)
