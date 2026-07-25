package io.github.kei_1111.app.core.common.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class LogLevel(val letter: Char) {
    Debug('D'),
    Info('I'),
    Warn('W'),
    Error('E'),
}

data class LogEntry(
    /** 実 Logcat 形式の発生時刻（`yyyy-MM-dd HH:mm:ss.SSS`、ローカル時刻）。 */
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
)

/** 現在時刻を Logcat 形式で返す。ローカルタイムゾーンの解決が必要なためプラットフォーム実装に委ねる。 */
internal expect fun currentLogTimestamp(): String

/**
 * 画面やモジュールを横断する操作履歴を保持するアプリスコープのロガー。
 * 発生元と表示先が異なるため、画面ローカルの MVI ではなくシングルトンで共有する。
 */
object InteractionLog {
    private const val MAX_ENTRIES = 300

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun d(tag: String, message: String) = append(LogLevel.Debug, tag, message)

    fun i(tag: String, message: String) = append(LogLevel.Info, tag, message)

    fun w(tag: String, message: String) = append(LogLevel.Warn, tag, message)

    fun e(tag: String, message: String) = append(LogLevel.Error, tag, message)

    fun clear() {
        _entries.value = emptyList()
    }

    private fun append(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = currentLogTimestamp(),
            level = level,
            tag = tag,
            message = message,
        )
        _entries.update { (it + entry).takeLast(MAX_ENTRIES) }
    }
}
