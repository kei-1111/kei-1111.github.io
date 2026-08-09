package io.github.kei_1111.app.feature.profile.destination.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kei_1111.app.core.designsystem.language.KeiLanguageController
import io.github.kei_1111.app.core.designsystem.theme.KeiTheme
import io.github.kei_1111.app.core.mvi.MviEffect
import io.github.kei_1111.app.core.utils.DoubleShiftEffect
import io.github.kei_1111.app.core.utils.openUrl

@Composable
internal fun ProfileScreenRoot(
    viewModel: ProfileViewModel,
    navigateSearchEverywhere: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = KeiTheme.colors.isDark

    MviEffect(
        effect = state.effect,
        onConsume = { viewModel.onIntent(ProfileIntent.ConsumeEffect) },
    ) { effect ->
        when (effect) {
            is ProfileEffect.NavigateSearchEverywhere -> navigateSearchEverywhere()
            is ProfileEffect.OpenUrl -> openUrl(effect.url)

            // テーマ / 言語の状態は App / KeiLanguageController が所有するため、目標値と現在値が
            // 異なるときだけ既存のトグルコールバックを叩く（ViewModel 側の判定と二重の安全弁）
            is ProfileEffect.SwitchTheme -> if (effect.isDark != isDark) onToggleTheme()
            is ProfileEffect.SwitchLanguage ->
                if (effect.language != KeiLanguageController.language) onToggleLanguage()
        }
    }

    // theme コマンドの判定用に、App が所有するテーマ状態を ViewModel へ同期する（UpdateLayout と同じ環境プッシュ）
    LaunchedEffect(isDark) { viewModel.onIntent(ProfileIntent.UpdateTheme(isDark)) }

    DoubleShiftEffect { viewModel.onIntent(ProfileIntent.OpenSearchEverywhere) }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onToggleTheme = onToggleTheme,
        onToggleLanguage = onToggleLanguage,
        modifier = modifier,
    )
}
