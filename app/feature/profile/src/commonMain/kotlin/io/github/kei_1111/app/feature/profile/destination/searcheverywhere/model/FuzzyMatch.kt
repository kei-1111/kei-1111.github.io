package io.github.kei_1111.app.feature.profile.destination.searcheverywhere.model

private const val MATCH_SCORE = 1
private const val CONSECUTIVE_BONUS = 2
private const val WORD_START_BONUS = 3
private const val CANDIDATE_START_BONUS = 5
private const val NO_MATCH = -1

/** 直前の一致位置の初期値。0 文字目の一致を「連続」と誤判定しないよう -1 より手前に置く。 */
private const val NO_PREVIOUS_MATCH = -2
private const val WORD_SEPARATORS = " ./_-›"

/**
 * クエリの各文字が候補内に順番どおり現れれば一致とみなし、スコアを返す（不一致は null）。
 * 呼出側が空クエリを除外する前提のため、空クエリは全一致としてスコア 0 を返す。
 */
internal fun fuzzyScore(query: String, candidate: String): Int? {
    var candidateIndex = 0
    var previousMatch = NO_PREVIOUS_MATCH
    var firstMatch = NO_MATCH
    var score = 0

    for (queryCharacter in query) {
        while (candidateIndex < candidate.length && !candidate[candidateIndex].equals(queryCharacter, ignoreCase = true)) {
            candidateIndex++
        }
        if (candidateIndex == candidate.length) return null

        if (firstMatch == NO_MATCH) firstMatch = candidateIndex
        score += MATCH_SCORE
        if (candidateIndex == previousMatch + 1) score += CONSECUTIVE_BONUS
        if (candidate.isWordStartAt(candidateIndex)) score += WORD_START_BONUS
        previousMatch = candidateIndex
        candidateIndex++
    }

    return score + if (firstMatch == 0) CANDIDATE_START_BONUS else 0
}

private fun String.isWordStartAt(index: Int): Boolean {
    if (index == 0) return true
    val previous = this[index - 1]
    return previous in WORD_SEPARATORS || (previous.isLowerCase() && this[index].isUpperCase())
}
