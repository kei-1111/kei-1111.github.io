@file:Suppress("MagicNumber", "ModifierMissing", "TooManyFunctions", "UnusedPrivateMember")

package io.github.kei_1111.app.feature.profile.destination.profile.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kei_1111.app.core.designsystem.theme.CodeJapaneseFallbackFamily
import io.github.kei_1111.app.core.designsystem.theme.KeiIcon
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.designsystem.theme.ThemedIcon
import io.github.kei_1111.app.feature.profile.destination.profile.EditorPage
import io.github.kei_1111.app.feature.profile.destination.profile.EditorViewMode
import io.github.kei_1111.app.feature.profile.destination.profile.component.markdown.highlightMarkdownBuffer
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewGitHubProfile
import io.github.kei_1111.app.feature.profile.destination.profile.preview.PreviewThirdPartyLicenses
import io.github.kei_1111.app.feature.profile.destination.profile.profileCode
import io.github.kei_1111.app.feature.profile.theme.ProfileAnimations
import io.github.kei_1111.app.feature.profile.theme.ProfileDimensions
import io.github.kei_1111.app.feature.profile.theme.highlightBuffer
import io.github.kei_1111.app.feature.profile.theme.rememberHoverState
import io.github.kei_1111.shared.model.GitHubProfile
import io.github.kei_1111.shared.model.ThirdPartyLicenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop

/** 読み取り専用ファイルであることを示す減光。 */
private const val LOCKED_CODE_ALPHA = 0.6f

/**
 * エディタのタブバー。[viewMode] と [onChangeViewMode] を渡すと、
 * 右端に実 AS の Code / Split / Design 相当の表示モード切替を表示する。
 * タブ列は weight で残り幅に収め、幅が足りないときは横スクロールする
 * （右端のボタン群が画面外に押し出されないようにするため）。
 * 選択中またはホバー中のタブには閉じるボタンを表示し、全状態でその幅を確保する。
 * [showSplitButton] を false にすると Split ボタンを表示しない（Mobile 用）。
 */
@Composable
internal fun EditorTabBar(
    openPages: ImmutableList<EditorPage>,
    selectedPage: EditorPage?,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
    viewMode: EditorViewMode? = null,
    onChangeViewMode: ((EditorViewMode) -> Unit)? = null,
    showSplitButton: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabList(
            openPages = openPages,
            selectedPage = selectedPage,
            onClickPage = onClickPage,
            onClosePage = onClosePage,
            modifier = Modifier.weight(1f),
        )
        if (viewMode != null && onChangeViewMode != null) {
            TabListIndicator()
            Spacer(modifier = Modifier.width(4.dp))
            ViewModeButton(
                icon = KeiTheme.icons.editorOnly,
                selected = viewMode == EditorViewMode.CodeOnly,
                onClick = { onChangeViewMode(EditorViewMode.CodeOnly) },
            )
            if (showSplitButton) {
                ViewModeButton(
                    icon = KeiTheme.icons.editorPreview,
                    selected = viewMode == EditorViewMode.Split,
                    onClick = { onChangeViewMode(EditorViewMode.Split) },
                )
            }
            ViewModeButton(
                icon = KeiTheme.icons.previewOnly,
                selected = viewMode == EditorViewMode.PreviewOnly,
                onClick = { onChangeViewMode(EditorViewMode.PreviewOnly) },
            )
            Spacer(modifier = Modifier.width(4.dp))
            EditorMenuIndicator()
        }
    }
}

/** 開いているタブの横スクロール列。 */
@Composable
private fun TabList(
    openPages: ImmutableList<EditorPage>,
    selectedPage: EditorPage?,
    onClickPage: (EditorPage) -> Unit,
    onClosePage: (EditorPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        openPages.forEach { page ->
            key(page) {
                EditorTab(
                    page = page,
                    selected = page == selectedPage,
                    onClick = { onClickPage(page) },
                    onClose = { onClosePage(page) },
                )
            }
        }
    }
}

/** タブ列右の装飾シェブロン。実 AS の隠れタブ一覧アイコンを模しただけで、クリックしても何も起きない。 */
@Composable
private fun TabListIndicator(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.chevronDown,
        contentDescription = null,
        modifier = modifier.size(12.dp),
    )
}

/** タブバー右端のエディタオプションメニューアイコン。 */
@Composable
private fun EditorMenuIndicator(modifier: Modifier = Modifier) {
    KeiIcon(
        icon = KeiTheme.icons.moreVertical,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

/** 表示モード切替の1ボタン。選択中はグレーの選択ピルで示す。 */
@Composable
private fun ViewModeButton(
    icon: ThemedIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(KeiTheme.shapes.chip)
            .background(if (selected) KeiTheme.colors.selectionPill else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

@Composable
private fun EditorTab(
    page: EditorPage,
    selected: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoverState = rememberHoverState()
    val background = when {
        selected -> KeiTheme.colors.tabSelected
        hoverState.hovered -> KeiTheme.colors.chip
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .clip(KeiTheme.shapes.row)
            .background(background)
            .then(
                if (selected) {
                    Modifier.border(1.dp, KeiTheme.colors.tabSelectedBorder, KeiTheme.shapes.row)
                } else {
                    Modifier
                },
            )
            .hoverable(hoverState.interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabFileIcon(kotlin = page.fileName.endsWith(".kt"))
        TabLabel(fileName = page.fileName, selected = selected)
        val closeVisible = selected || hoverState.hovered
        // 非表示時は clickable 自体を外す。enabled=false でもタップを consume して親のタブ選択を塞ぐため
        TabCloseIcon(onClick = onClose, visible = closeVisible)
    }
}

@Composable
private fun TabFileIcon(
    kotlin: Boolean,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = if (kotlin) KeiTheme.icons.kotlin else KeiTheme.icons.markdown,
        contentDescription = null,
        modifier = modifier.size(ProfileDimensions.ChromeIconSize),
    )
}

@Composable
private fun TabLabel(
    fileName: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = fileName,
        modifier = modifier,
        style = KeiTheme.typography.chrome.copy(
            fontSize = ProfileDimensions.ChromeLabelFontSize,
            color = if (selected) KeiTheme.colors.textPrimary else KeiTheme.colors.textSecondary,
        ),
    )
}

/** 選択中またはホバー中のタブに表示する閉じるボタン。非表示時も幅を確保する。 */
@Composable
private fun TabCloseIcon(
    onClick: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ProfileDimensions.ChromeIconSize)
            .clip(KeiTheme.shapes.chip)
            .alpha(if (visible) 1f else 0f)
            .then(if (visible) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        KeiIcon(
            icon = KeiTheme.icons.closeSmall,
            contentDescription = null,
            modifier = Modifier.size(ProfileDimensions.ChromeIconSize),
        )
    }
}

/**
 * エディタのコード領域（実 AS 風の縦横オーバーレイスクロールバー付き）。
 * Desktop の島レイアウトと Mobile の CodeOnly 表示から直接使う。
 * [editable] が true の Profile / README ページは編集可能フィールドを表示する。
 */
@Composable
internal fun EditorCodeArea(
    page: EditorPage,
    profile: GitHubProfile,
    licenses: ThirdPartyLicenses?,
    modifier: Modifier = Modifier,
    editorCode: String = "",
    editable: Boolean = false,
    onChangeCode: (String) -> Unit = {},
    codeHasError: Boolean = false,
    editorResetTick: Int = 0,
    locked: Boolean = false,
) {
    if (editable && (page == EditorPage.Profile || page == EditorPage.Readme)) {
        key(page) {
            EditableCodeArea(
                code = editorCode,
                resetTick = editorResetTick,
                onChangeCode = onChangeCode,
                hasError = codeHasError,
                markdown = page == EditorPage.Readme,
                modifier = modifier,
            )
        }
    } else {
        val japaneseFontFamily = CodeJapaneseFallbackFamily()
        val colors = KeiTheme.colors
        val lines = remember(page, profile, licenses, japaneseFontFamily, colors) {
            codeLinesFor(page, profile, licenses, japaneseFontFamily, colors)
        }
        ScrollableCodeArea(
            lines = lines,
            modifier = modifier.alpha(if (locked) LOCKED_CODE_ALPHA else 1f),
        )
    }
}

/** 全タブを閉じたときにエディタ島へ表示する、サイトの使い方のコード風ページ。 */
@Composable
internal fun UsageCodeArea(modifier: Modifier = Modifier) {
    val japaneseFontFamily = CodeJapaneseFallbackFamily()
    val colors = KeiTheme.colors
    val lines = remember(japaneseFontFamily, colors) { usageCodeLines(japaneseFontFamily, colors) }
    ScrollableCodeArea(lines = lines, modifier = modifier)
}

/** ハイライト済みコードを縦横スクロール可能なエディタ領域へ表示する。 */
@Composable
private fun ScrollableCodeArea(
    lines: List<AnnotatedString>,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var lineNumberWidthPx by remember { mutableIntStateOf(0) }
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState),
        ) {
            CodeLines(
                lines = lines,
                horizontalScrollState = horizontalScrollState,
                onLineNumberWidthChanged = { lineNumberWidthPx = it },
            )
        }
        VerticalScrollbar(
            scrollState = verticalScrollState,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        // 横スクロールの実ビューポートは行番号ガターを除いた CodeColumn 幅なので、その分だけ左に寄せる
        HorizontalScrollbar(
            scrollState = horizontalScrollState,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = with(LocalDensity.current) { lineNumberWidthPx.toDp() }),
        )
    }
}

/** 編集可能なコードを [ScrollableCodeArea] と同じスクロールバー付き領域へ表示する。 */
@Composable
private fun EditableCodeArea(
    code: String,
    resetTick: Int,
    onChangeCode: (String) -> Unit,
    hasError: Boolean,
    markdown: Boolean,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var lineNumberWidthPx by remember { mutableIntStateOf(0) }
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState),
        ) {
            EditableCodeLines(
                code = code,
                resetTick = resetTick,
                onChangeCode = onChangeCode,
                hasError = hasError,
                markdown = markdown,
                horizontalScrollState = horizontalScrollState,
                onLineNumberWidthChanged = { lineNumberWidthPx = it },
            )
        }
        VerticalScrollbar(
            scrollState = verticalScrollState,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        HorizontalScrollbar(
            scrollState = horizontalScrollState,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = with(LocalDensity.current) { lineNumberWidthPx.toDp() }),
        )
    }
}

/** 行番号 + 編集可能なコード + 自前キャレットを自然な高さで描画する（縦スクロールは持たない）。 */
@Composable
private fun EditableCodeLines(
    code: String,
    resetTick: Int,
    onChangeCode: (String) -> Unit,
    hasError: Boolean,
    markdown: Boolean,
    modifier: Modifier = Modifier,
    horizontalScrollState: ScrollState = rememberScrollState(),
    onLineNumberWidthChanged: (Int) -> Unit = {},
) {
    val japaneseFontFamily = CodeJapaneseFallbackFamily()
    val colors = KeiTheme.colors
    // リセット時のみ作り直す。編集中のキーストロークでは同一インスタンスを維持する
    val textFieldState = remember(resetTick) { TextFieldState(code) }
    val currentOnChangeCode by rememberUpdatedState(onChangeCode)
    LaunchedEffect(textFieldState) {
        // drop(1): 初期テキストはユーザ編集ではないため通知しない
        snapshotFlow { textFieldState.text.toString() }.drop(1).collect { currentOnChangeCode(it) }
    }
    val highlight = remember(markdown, japaneseFontFamily, colors) {
        OutputTransformation {
            if (markdown) {
                highlightMarkdownBuffer(this, japaneseFontFamily, colors)
            } else {
                highlightBuffer(this, japaneseFontFamily, colors)
            }
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState()
    val blinkVisible = rememberCaretBlink(textFieldState)
    var textLayout by remember { mutableStateOf<() -> TextLayoutResult?>({ null }) }

    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            // 実 AS 同様、キャレット行はガターを含む全幅をハイライトする
            .drawBehind {
                val layout = textLayout() ?: return@drawBehind
                // selection がレイアウトより新しい瞬間の範囲外クラッシュを防ぐ
                val offset = textFieldState.selection.start.coerceIn(0, layout.layoutInput.text.length)
                val line = layout.getLineForOffset(offset)
                val top = layout.getLineTop(line)
                drawRect(
                    color = colors.editorCaretRow,
                    topLeft = Offset(0f, top),
                    size = Size(size.width, layout.getLineBottom(line) - top),
                )
            },
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            LineNumberColumn(
                lineCount = textFieldState.text.count { it == '\n' } + 1,
                modifier = Modifier.onSizeChanged { onLineNumberWidthChanged(it.width) },
            )
            BasicTextField(
                state = textFieldState,
                modifier = Modifier
                    .weight(1f)
                    // 無限幅測定になり折り返しを防ぐ
                    .horizontalScroll(horizontalScrollState)
                    .editorCaret(
                        state = textFieldState,
                        color = KeiTheme.colors.textPrimary,
                        visible = { focused.value && blinkVisible.value },
                        textLayout = { textLayout() },
                    ),
                textStyle = KeiTheme.typography.code.copy(color = colors.textCode),
                // 標準カーソルは太さを変更できないため透明にし、editorCaret で自前描画する
                cursorBrush = SolidColor(Color.Transparent),
                outputTransformation = highlight,
                lineLimits = TextFieldLineLimits.MultiLine(),
                interactionSource = interactionSource,
                onTextLayout = { getResult -> textLayout = getResult },
            )
        }
        InspectionsIndicator(hasError = hasError, modifier = Modifier.align(Alignment.TopEnd))
    }
}

/**
 * 実 AS のキャレット点滅（step-end）。入力やキャレット移動のたびに点灯状態へリセットされる。
 * 値は draw フェーズでだけ読む前提（点滅ごとの再コンポーズを避ける）。
 */
@Composable
private fun rememberCaretBlink(textFieldState: TextFieldState): State<Boolean> {
    val visible = remember { mutableStateOf(true) }
    LaunchedEffect(textFieldState.selection) {
        visible.value = true
        while (true) {
            delay(ProfileAnimations.CaretBlinkMillis / 2L)
            visible.value = !visible.value
        }
    }
    return visible
}

/**
 * AS 風キャレット（[ProfileDimensions.EditorCaretWidth] 幅、上下 [ProfileDimensions.EditorCaretVerticalInset]
 * を空けた高さ）を選択位置へ自前描画する。範囲選択中は描画しない。
 */
private fun Modifier.editorCaret(
    state: TextFieldState,
    color: Color,
    visible: () -> Boolean,
    textLayout: () -> TextLayoutResult?,
): Modifier = drawWithContent {
    drawContent()
    if (!visible() || !state.selection.collapsed) return@drawWithContent
    val layout = textLayout() ?: return@drawWithContent
    // selection がレイアウトより新しい瞬間の範囲外クラッシュを防ぐ
    val rect = layout.getCursorRect(state.selection.start.coerceIn(0, layout.layoutInput.text.length))
    val inset = ProfileDimensions.EditorCaretVerticalInset.toPx()
    drawRect(
        color = color,
        topLeft = Offset(rect.left, rect.top + inset),
        size = Size(ProfileDimensions.EditorCaretWidth.toPx(), rect.height - inset * 2),
    )
}

/** 行番号 + ハイライト済みコード + キャレットを自然な高さで描画する（縦スクロールは持たない）。 */
@Composable
private fun CodeLines(
    lines: List<AnnotatedString>,
    horizontalScrollState: ScrollState = rememberScrollState(),
    onLineNumberWidthChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val caretRowColor = KeiTheme.colors.editorCaretRow
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            // 疑似キャレットのある最終行を、実 AS のキャレット行と同様にハイライトする
            .drawBehind {
                val lineHeight = ProfileDimensions.EditorLineHeight.toPx()
                drawRect(
                    color = caretRowColor,
                    topLeft = Offset(0f, lines.lastIndex * lineHeight),
                    size = Size(size.width, lineHeight),
                )
            },
    ) {
        CodeBody(
            lines = lines,
            horizontalScrollState = horizontalScrollState,
            onLineNumberWidthChanged = onLineNumberWidthChanged,
            modifier = Modifier.fillMaxWidth(),
        )
        InspectionsIndicator(modifier = Modifier.align(Alignment.TopEnd))
    }
}

/** 行番号ガター + ハイライト済みコード列。 */
@Composable
private fun CodeBody(
    lines: List<AnnotatedString>,
    horizontalScrollState: ScrollState = rememberScrollState(),
    onLineNumberWidthChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        LineNumberColumn(
            lineCount = lines.size,
            modifier = Modifier.onSizeChanged { onLineNumberWidthChanged(it.width) },
        )
        CodeColumn(
            lines = lines,
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScrollState),
        )
    }
}

@Composable
private fun LineNumberColumn(
    lineCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier.padding(start = 12.dp, end = 12.dp),
    ) {
        repeat(lineCount) { i ->
            Text(
                text = (i + 1).toString(),
                modifier = Modifier.height(ProfileDimensions.EditorLineHeight),
                style = KeiTheme.typography.code.copy(color = KeiTheme.colors.muted),
                textAlign = TextAlign.End,
            )
        }
    }
}

/** ハイライト済みコード列（横スクロール）。 */
@Composable
private fun CodeColumn(
    lines: List<AnnotatedString>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        lines.forEachIndexed { i, line ->
            Row(
                modifier = Modifier.height(ProfileDimensions.EditorLineHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CodeLineText(line = line)
                if (i == lines.lastIndex) {
                    BlinkingCaret()
                }
            }
        }
    }
}

@Composable
private fun CodeLineText(
    line: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(text = line, modifier = modifier, style = KeiTheme.typography.code, softWrap = false)
}

/** 右上に表示するインスペクション状態。 */
@Composable
private fun InspectionsIndicator(
    hasError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    KeiIcon(
        icon = if (hasError) KeiTheme.icons.inspectionsError else KeiTheme.icons.inspectionsOk,
        contentDescription = null,
        modifier = modifier
            .padding(top = 8.dp, end = 14.dp)
            .size(ProfileDimensions.ChromeIconSize),
    )
}

/** 点滅キャレット（8×15px, 1.1s step-end 相当）。 */
@Composable
private fun BlinkingCaret(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ProfileAnimations.CaretBlinkMillis,
                easing = { fraction -> if (fraction < 0.5f) 0f else 1f },
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Box(
        modifier = modifier
            .padding(start = 1.dp)
            .alpha(alpha)
            .size(width = 8.dp, height = 15.dp)
            .background(KeiTheme.colors.textPrimary),
    )
}

@Preview
@Composable
private fun EditorTabBarPreview() {
    KeiTheme {
        Box(modifier = Modifier.background(KeiTheme.colors.island)) {
            EditorTabBar(
                openPages = persistentListOf(EditorPage.Profile, EditorPage.Licenses),
                selectedPage = EditorPage.Profile,
                onClickPage = {},
                onClosePage = {},
                viewMode = EditorViewMode.Split,
                onChangeViewMode = {},
            )
        }
    }
}

@Preview
@Composable
private fun EditorCodeAreaPreview() {
    KeiTheme {
        // verticalScroll は無限制約下で測定できないため、Preview では有限サイズを与える
        Box(
            modifier = Modifier
                .size(width = 560.dp, height = 480.dp)
                .background(KeiTheme.colors.island),
        ) {
            EditorCodeArea(page = EditorPage.Profile, profile = PreviewGitHubProfile, licenses = PreviewThirdPartyLicenses)
        }
    }
}

@Preview
@Composable
private fun CodeLinesPreview() {
    KeiTheme {
        val japaneseFontFamily = CodeJapaneseFallbackFamily()
        val colors = KeiTheme.colors
        val lines = remember(
            PreviewGitHubProfile,
            PreviewThirdPartyLicenses,
            japaneseFontFamily,
            colors,
        ) {
            codeLinesFor(
                EditorPage.Profile,
                PreviewGitHubProfile,
                PreviewThirdPartyLicenses,
                japaneseFontFamily,
                colors,
            )
        }
        Box(
            modifier = Modifier
                .width(560.dp)
                .background(KeiTheme.colors.island),
        ) {
            CodeLines(lines = lines)
        }
    }
}

@Preview
@Composable
private fun UsageCodeAreaPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .size(width = 560.dp, height = 480.dp)
                .background(KeiTheme.colors.island),
        ) {
            UsageCodeArea()
        }
    }
}

@Preview
@Composable
private fun EditableCodeLinesPreview() {
    KeiTheme {
        Box(
            modifier = Modifier
                .width(560.dp)
                .background(KeiTheme.colors.island),
        ) {
            EditableCodeLines(
                code = profileCode(PreviewGitHubProfile),
                resetTick = 0,
                onChangeCode = {},
                hasError = false,
                markdown = false,
            )
        }
    }
}
