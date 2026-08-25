@file:Suppress("MagicNumber")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kei_1111.app.core.designsystem.component.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.utils.prefersReducedMotion
import io.github.kei_1111.app.feature.profile.destination.profile.model.TERMINAL_PROMPT
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLine
import io.github.kei_1111.app.feature.profile.destination.profile.model.TerminalLineKind
import io.github.kei_1111.app.feature.profile.destination.profile.theme.ProfileDimensions
import io.github.kei_1111.test.tags.TestTags
import kei_1111.app.feature.profile.generated.resources.Res
import kei_1111.app.feature.profile.generated.resources.terminal_close
import kei_1111.app.feature.profile.generated.resources.terminal_hide
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/** 実 AS New UI の Terminal ツールウィンドウを模した疑似シェル。 */
@Composable
internal fun TerminalPanel(
    lines: ImmutableList<TerminalLine>,
    input: String,
    onChangeInput: (String) -> Unit,
    onExecuteCommand: () -> Unit,
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    // 行追加直後はレイアウト前で maxValue が古いため、スクロール範囲の確定後に末尾へ追従する。
    // 実ターミナル同様、直前に最下部へいたときだけ追従し、上へスクロール中の閲覧は奪わない
    var lastMaxValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(scrollState.maxValue) {
        // 初回レイアウト前の maxValue は Int.MAX_VALUE（Scrollbar.kt と同じ既知の初期値）なので読み飛ばす
        if (scrollState.maxValue == Int.MAX_VALUE) return@LaunchedEffect
        val wasAtBottom = scrollState.value >= lastMaxValue
        lastMaxValue = scrollState.maxValue
        if (wasAtBottom) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .clip(KeiTheme.shapes.island)
            .background(KeiTheme.colors.island),
    ) {
        TerminalHeader(
            onClickHide = onClickHide,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { focusRequester.requestFocus() }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .horizontalScroll(horizontalScrollState)
                    // 横スクロール下では幅制約が無限になり入力行の weight(1f) が幅 0 に潰れるため、
                    // ProjectTree と同様に「パネル幅と最長行の大きい方」へ幅を束縛する
                    .width(IntrinsicSize.Max)
                    .padding(start = 12.dp, top = 4.dp, end = 8.dp, bottom = 8.dp),
            ) {
                lines.forEach { line ->
                    Text(
                        text = line.text.ifEmpty { " " },
                        style = KeiTheme.typography.code.copy(
                            color = when (line.kind) {
                                TerminalLineKind.Command,
                                TerminalLineKind.Output,
                                -> KeiTheme.colors.textPrimary

                                TerminalLineKind.Error -> KeiTheme.colors.logcatError
                                TerminalLineKind.Success -> KeiTheme.colors.androidGreen
                            },
                        ),
                        softWrap = false,
                        maxLines = 1,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = TERMINAL_PROMPT,
                        style = KeiTheme.typography.code.copy(color = KeiTheme.colors.textPrimary),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TerminalInput(
                        input = input,
                        onChangeInput = onChangeInput,
                        onExecuteCommand = onExecuteCommand,
                        focusRequester = focusRequester,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            VerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
            HorizontalScrollbar(
                scrollState = horizontalScrollState,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun TerminalHeader(
    onClickHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Terminal",
            modifier = Modifier.semantics { heading() },
            style = KeiTheme.typography.chrome.copy(
                color = KeiTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        TerminalTab(onClose = onClickHide)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "+",
            style = KeiTheme.typography.chrome.copy(fontSize = 14.sp),
            modifier = Modifier.alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.width(6.dp))
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSizeSmall)
                .alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "AI Agents",
            style = KeiTheme.typography.chrome,
            modifier = Modifier.alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.width(4.dp))
        KeiIcon(
            icon = KeiTheme.icons.chevronDown,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSizeSmall)
                .alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.width(8.dp))
        KeiIcon(
            icon = KeiTheme.icons.moreVertical,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSize)
                .alpha(KeiTheme.colors.nonClickableAlpha),
        )
        Spacer(modifier = Modifier.width(2.dp))
        ChromeIconButton(
            icon = KeiTheme.icons.toolWindowHide,
            contentDescription = stringResource(Res.string.terminal_hide),
            modifier = Modifier.testTag(TestTags.Profile.TERMINAL_HIDE),
            iconSize = ProfileDimensions.ChromeIconSize,
            onClick = onClickHide,
        )
    }
}

/** エディタの選択タブと同じ青ピルで描く。 */
@Composable
private fun TerminalTab(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(KeiTheme.colors.tabSelected)
            .border(1.dp, KeiTheme.colors.tabSelectedBorder, KeiTheme.shapes.row)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Local",
            style = KeiTheme.typography.chrome.copy(color = KeiTheme.colors.textPrimary),
        )
        KeiIcon(
            icon = KeiTheme.icons.closeSmall,
            contentDescription = stringResource(Res.string.terminal_close),
            modifier = Modifier
                .size(ProfileDimensions.ChromeIconSizeSmall)
                .clip(KeiTheme.shapes.chip)
                .testTag(TestTags.Profile.TERMINAL_TAB_CLOSE)
                .clickable(onClick = onClose),
        )
    }
}

@Composable
private fun TerminalInput(
    input: String,
    onChangeInput: (String) -> Unit,
    onExecuteCommand: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    // EditorPane の TextFieldState 方式と異なり value/onValueChange のミラーを使う: 入力テキストの
    // 所有者は ViewModel（実行時にクリアされる）で、選択位置だけが View ローカルなため。この同期は
    // onChangeInput の往復が次のリコンポジションまでに反映される前提に立つ（debounce を挟むと壊れる）
    var fieldValue by remember { mutableStateOf(TextFieldValue(input, TextRange(input.length))) }
    if (fieldValue.text != input) {
        // ViewModel 側で入力が確定・クリアされたらローカルの選択位置ごと追従させる
        fieldValue = fieldValue.copy(text = input, selection = TextRange(input.length))
    }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var caretVisible by remember { mutableStateOf(true) }
    val reducedMotion = remember { prefersReducedMotion() }
    val caretColor = KeiTheme.colors.textPrimary
    val textStyle = KeiTheme.typography.code.copy(color = KeiTheme.colors.textPrimary)
    LaunchedEffect(focused, fieldValue, reducedMotion) {
        caretVisible = true
        if (focused && !reducedMotion) {
            while (true) {
                delay(500)
                caretVisible = !caretVisible
            }
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            if (it.text != input) onChangeInput(it.text)
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .testTag(TestTags.Profile.TERMINAL_INPUT)
            .onPreviewKeyEvent { event ->
                if (
                    event.type == KeyEventType.KeyDown &&
                    (event.key == Key.Enter || event.key == Key.NumPadEnter)
                ) {
                    onExecuteCommand()
                    true
                } else {
                    false
                }
            }
            .drawWithContent {
                drawContent()
                val layout = layoutResult
                // EditorPane のキャレットと同様、範囲選択中はブロックキャレットを描かない
                if (caretVisible && layout != null && fieldValue.selection.collapsed) {
                    // selection がレイアウトより新しい瞬間の範囲外クラッシュを防ぐ（EditorPane のキャレットと同じガード）
                    val offset = fieldValue.selection.end.coerceIn(0, layout.layoutInput.text.length)
                    val rect = layout.getCursorRect(offset)
                    // 実 AS（JediTerm）のブロックカーソルは1文字セル幅。JetBrains Mono の字送りは 0.6em
                    drawRect(
                        color = caretColor,
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(textStyle.fontSize.toPx() * 0.6f, rect.height),
                    )
                }
            },
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(Color.Transparent),
        interactionSource = interactionSource,
        onTextLayout = { layoutResult = it },
    )
}

@Preview
@Composable
private fun TerminalPanelPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 900.dp, height = ProfileDimensions.TerminalPanelHeight)
                .background(KeiTheme.colors.desk),
        ) {
            TerminalPanel(
                lines = persistentListOf(
                    TerminalLine("$TERMINAL_PROMPT help", TerminalLineKind.Command),
                    TerminalLine("Available commands:", TerminalLineKind.Output),
                    TerminalLine(
                        "  open <target>     open a file or link (readme|profile|works|licenses|github|x|qiita|note)",
                        TerminalLineKind.Output,
                    ),
                    TerminalLine("zsh: command not found: foo", TerminalLineKind.Error),
                    TerminalLine("BUILD SUCCESSFUL in 2s", TerminalLineKind.Success),
                ),
                input = "",
                onChangeInput = {},
                onExecuteCommand = {},
                onClickHide = {},
            )
        }
    }
}
