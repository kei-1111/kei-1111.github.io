package org.example.project.data

import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_profile_icon

data object BasicInfoSet {
    val profileIcon = Res.drawable.img_profile_icon
    const val name = "けい"
    const val birthday = "2002/11/11"
    const val university = "公立はこだて未来大学 学部4年"
    val introduction = """
                初めてAndroidアプリ開発したときの、作ったものがスマホに表示された感動が忘れられず、Android開発をやってます。
                将来的には、誰もが使ったことのあるようなアプリを作りたいと思っています！
    """.trimIndent()
}
