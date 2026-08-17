package io.github.kei_1111.app.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.kei_1111.app.core.common.dispatcher.DefaultDispatcher
import io.github.kei_1111.app.core.local.language.LanguageLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

interface LanguageRepository {
    /**
     * 保存された表示言語の BCP 47 タグ。未保存時と読み取り失敗時は null で、テーマと違い固定の
     * 初期値へ丸めない — 未保存はブラウザロケール検出へ委ねる合図であり、呼び出し側が解決する。
     */
    val languageTag: Flow<String?>

    suspend fun saveLanguageTag(languageTag: String)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class LanguageRepositoryImpl(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val languageLocalDataSource: LanguageLocalDataSource,
) : LanguageRepository {

    override val languageTag: Flow<String?> = languageLocalDataSource.languageTag
        .flowOn(defaultDispatcher)

    override suspend fun saveLanguageTag(languageTag: String) {
        languageLocalDataSource.saveLanguageTag(languageTag)
    }
}
