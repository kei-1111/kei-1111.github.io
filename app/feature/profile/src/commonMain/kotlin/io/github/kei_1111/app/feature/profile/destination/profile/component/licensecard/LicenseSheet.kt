@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component.licensecard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewThirdPartyLicenses
import io.github.kei_1111.app.feature.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.shared.model.LicenseEntry
import io.github.kei_1111.shared.model.LicenseType
import kotlinx.collections.immutable.ImmutableMap

/**
 * ライセンス全文をプレビューカード内に表示するボトムシート型オーバーレイ。
 * ウィンドウ全体ではなく LicenseScreenPreview（カード）の中に、スクリムとシートを重ねて描画する。
 * ナビゲーション destination ではなく、Profile 画面が state として持つ選択中ライセンス
 * （[LicenseEntry]）に紐づく画面内コンポーネント（`.claude/rules/navigation.md` 参照）。
 */
@Composable
internal fun LicenseSheetOverlay(
    license: LicenseEntry?,
    texts: ImmutableMap<LicenseType, String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 閉じるアニメーション中も直前の内容を描き続けるため、最後に表示したエントリを保持する
    var lastLicense by remember { mutableStateOf<LicenseEntry?>(null) }
    if (license != null) lastLicense = license
    val shown = lastLicense ?: return

    Box(modifier = modifier) {
        SheetScrim(
            visible = license != null,
            onClickScrim = onDismiss,
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = license != null,
            enter = slideInVertically(tween(ProfileAnimations.SheetTransitionMillis)) { it } +
                fadeIn(tween(ProfileAnimations.SheetTransitionMillis)),
            exit = slideOutVertically(tween(ProfileAnimations.SheetTransitionMillis)) { it } +
                fadeOut(tween(ProfileAnimations.SheetTransitionMillis)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            LicenseSheet(
                license = shown,
                licenseText = texts[shown.type].orEmpty(),
                onClickClose = onDismiss,
            )
        }
    }
}

@Composable
private fun SheetScrim(
    visible: Boolean,
    onClickScrim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(ProfileAnimations.SheetTransitionMillis)),
        exit = fadeOut(tween(ProfileAnimations.SheetTransitionMillis)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KeiTheme.colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClickScrim,
                ),
        )
    }
}

@Composable
private fun LicenseSheet(
    license: LicenseEntry,
    licenseText: String,
    onClickClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(SHEET_HEIGHT_FRACTION)
            .clip(KeiTheme.shapes.sheet)
            .background(KeiTheme.colors.island),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SheetDragHandle()
        SheetHeader(
            license = license,
            onClickClose = onClickClose,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
        )
        HorizontalDivider(color = KeiTheme.colors.outline)
        SheetBody(
            license = license,
            licenseText = licenseText,
            modifier = Modifier.weight(1f),
        )
        SheetFooter(
            onClickClose = onClickClose,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

/** シート高さ（カード高さに対する割合）。 */
private const val SHEET_HEIGHT_FRACTION = 0.62f

@Composable
private fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(width = 28.dp, height = 3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(KeiTheme.colors.selectionPill),
    )
}

@Composable
private fun SheetHeader(
    license: LicenseEntry,
    onClickClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = license.name,
                style = KeiTheme.typography.chrome.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KeiTheme.colors.textPrimary,
                ),
            )
            Text(
                text = "${license.owner} · ${license.type.fullName}",
                style = KeiTheme.typography.chrome.copy(fontSize = 7.sp, color = KeiTheme.colors.syntaxString),
            )
        }
        SheetCloseButton(onClick = onClickClose)
    }
}

@Composable
private fun SheetCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(KeiTheme.colors.gitHubItem)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.closeSmall,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun SheetBody(
    license: LicenseEntry,
    licenseText: String,
    modifier: Modifier = Modifier,
) {
    val displayText = remember(licenseText) { reflowLicenseText(licenseText) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = license.copyright,
            style = KeiTheme.typography.chrome.copy(
                fontSize = 8.sp,
                lineHeight = 14.sp,
                color = KeiTheme.colors.textCode,
            ),
        )
        Text(
            text = displayText,
            style = KeiTheme.typography.chrome.copy(
                fontSize = 8.sp,
                lineHeight = 14.sp,
                color = KeiTheme.colors.textSecondary,
            ),
        )
    }
}

/** 全大文字の短い見出し行（PREAMBLE / DEFINITIONS など）。段落先頭に現れたときだけ独立行として扱う。 */
private val headingRegex = Regex("[A-Z][A-Z &,'-]*")

/** 箇条書きの先頭（a) / (i) / 1. など）。段落の途中でも改行して項目を分ける。 */
private val listItemRegex = Regex("""([a-z]{1,3}\)|\(\w{1,3}\)|\d+\.)\s.*""")

private const val HEADING_MAX_LENGTH = 40

/**
 * 80桁固定で整形されたライセンス原文を、シート幅で自然に折り返せるよう表示用に再整形する。
 * 空行区切りの段落内のハード改行を連結し、罫線・見出し・箇条書きだけを独立行に保つ。
 * データ層が持つ原文自体は変更しない（表示専用の変換）。
 */
private fun reflowLicenseText(text: String): String = buildString {
    var lineOpen = false
    text.lineSequence().forEach { raw ->
        val line = raw.trim()
        val standalone = line.isNotEmpty() &&
            (
                line.all { it == '-' } ||
                    (!lineOpen && line.length <= HEADING_MAX_LENGTH && headingRegex.matches(line))
                )
        when {
            line.isEmpty() -> if (lineOpen) {
                append("\n\n")
                lineOpen = false
            }

            standalone -> {
                if (lineOpen) append('\n')
                append(line).append('\n')
                lineOpen = false
            }

            lineOpen && listItemRegex.matches(line) -> append('\n').append(line)

            else -> {
                if (lineOpen) append(' ')
                append(line)
                lineOpen = true
            }
        }
    }
}.trim()

@Composable
private fun SheetFooter(
    onClickClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(KeiTheme.shapes.githubItem)
            .border(1.dp, KeiTheme.colors.selectionPill, KeiTheme.shapes.githubItem)
            .clickable(onClick = onClickClose),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "閉じる",
            style = KeiTheme.typography.cardJp.copy(fontSize = 10.sp),
        )
    }
}

@Preview
@Composable
private fun LicenseSheetOverlayPreview() {
    KeiTheme {
        val entry = PreviewThirdPartyLicenses.app.first()
        Box(
            modifier = Modifier
                .size(
                    width = ProfileDimensions.LicenseCardWidth,
                    height = ProfileDimensions.LicenseCardHeight,
                )
                .background(KeiTheme.colors.cardBackground),
        ) {
            LicenseSheetOverlay(
                license = entry,
                texts = PreviewThirdPartyLicenses.texts,
                onDismiss = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
