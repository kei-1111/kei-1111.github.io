package io.github.kei_1111.shared.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

/**
 * サードパーティライセンス情報。client 専用の静的コンテンツであり、
 * client / server 間の JSON 契約には含まれない（[GitHubProfile] と異なり @Serializable を付与しない）。
 */
enum class LicenseType(val id: String, val fullName: String) {
    Apache20(id = "Apache-2.0", fullName = "Apache License 2.0"),
    Ofl11(id = "OFL-1.1", fullName = "SIL Open Font License 1.1"),
    Epl10(id = "EPL-1.0", fullName = "Eclipse Public License 1.0"),
}

data class LicenseEntry(
    val name: String,
    val owner: String,
    val type: LicenseType,
    val url: String,
    val copyright: String,
)

data class ThirdPartyLicenses(
    val icons: ImmutableList<LicenseEntry>,
    val fonts: ImmutableList<LicenseEntry>,
    val app: ImmutableList<LicenseEntry>,
    /** サーバー（Cloud Run）側でのみ使う OSS。wasm 配布物には含まれない。 */
    val server: ImmutableList<LicenseEntry>,
    /** ライセンス全文。種別ごとに1つ持ち、同種別のエントリで共有する。 */
    val texts: ImmutableMap<LicenseType, String>,
)
