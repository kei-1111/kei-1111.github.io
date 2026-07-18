@file:Suppress("MagicNumber")

package io.github.kei_1111.app.core.data.contributions

import io.github.kei_1111.shared.model.ContributionCalendar
import io.github.kei_1111.shared.model.ContributionDay
import kotlinx.collections.immutable.toImmutableList

/**
 * API 取得失敗時に使う静的スナップショット（2026-07-04 時点の kei-1111 の実データ）。
 * 日付は開始日からの連番で復元する。
 */
internal object FallbackContributions {

    private const val START_YEAR = 2025
    private const val START_MONTH = 6
    private const val START_DAY = 29

    private const val LEVELS =
        "2423322422122012221211210101010121012101101111113111111101321101010000010011011111211222221233122212" +
            "2122211101101211224341132101213231223211121022322413241233243124212323232122211111121122131120123222" +
            "1122111332021110111112221221112123223331122210100101110010010032100111111111011011000000000000111221" +
            "22131114211211110232101122311221012011111111121110002111011111100011110"

    private const val COUNTS =
        "20,35,20,23,24,15,12,35,12,22,10,19,16,0,1,12,14,13,2,18,9,9,13,1,0,7,0,2,0,1,0,1,16,5,0,1,14,1,0,4," +
            "1,0,8,11,1,1,1,2,32,3,4,2,1,10,10,1,0,10,24,13,9,5,0,3,0,4,0,0,0,0,0,3,0,0,1,6,0,3,6,1,5,6,13,9,1,14," +
            "16,17,12,21,6,17,28,32,5,18,22,14,7,19,22,6,13,16,13,3,1,6,0,3,1,0,2,13,2,1,21,12,34,29,41,3,2,33,17," +
            "3,0,5,19,8,26,14,23,7,17,22,28,14,5,3,2,15,1,0,12,22,31,22,20,40,10,26,21,37,2,17,32,30,16,43,23,2," +
            "17,45,19,8,13,26,20,25,18,30,14,1,13,21,14,11,3,4,5,5,10,16,2,7,15,19,7,28,1,5,13,0,3,16,23,16,21,22," +
            "7,6,13,15,11,6,11,29,26,15,0,14,3,6,6,0,2,9,6,3,1,18,22,12,1,15,13,4,4,1,13,2,20,29,13,16,33,23,28," +
            "11,6,12,15,14,7,0,1,0,0,1,0,1,1,1,0,0,1,0,0,1,0,0,26,14,8,0,0,4,4,4,10,6,9,4,6,2,0,2,5,0,2,1,0,0,0,0," +
            "0,0,0,0,0,0,0,0,10,9,9,18,15,8,21,17,8,23,1,10,3,37,22,9,11,20,6,5,1,9,0,13,23,13,6,0,2,9,14,20,31," +
            "11,7,22,22,2,0,8,13,0,1,1,3,4,6,5,2,9,4,12,9,11,9,0,0,0,12,9,10,3,0,6,1,2,4,1,1,0,0,0,4,2,2,6,0"

    val calendar: ContributionCalendar by lazy {
        val counts = COUNTS.split(",").map { it.toInt() }
        val dates = buildDates(counts.size)
        val days = counts.mapIndexed { index, count ->
            ContributionDay(
                date = dates[index],
                count = count,
                level = LEVELS[index].digitToInt(),
            )
        }.toImmutableList()
        ContributionCalendar(totalLastYear = counts.sum(), days = days)
    }

    private fun buildDates(count: Int): List<String> {
        var year = START_YEAR
        var month = START_MONTH
        var day = START_DAY
        return List(count) {
            val date = "$year-${month.pad()}-${day.pad()}"
            day++
            if (day > daysInMonth(year, month)) {
                day = 1
                month++
                if (month > 12) {
                    month = 1
                    year++
                }
            }
            date
        }
    }

    private fun Int.pad() = toString().padStart(2, '0')

    private fun daysInMonth(year: Int, month: Int) = when (month) {
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }

    private fun isLeapYear(year: Int) = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}
