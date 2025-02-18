package org.example.project.data

import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_chatvox
import kei_1111.composeapp.generated.resources.img_poitto
import kei_1111.composeapp.generated.resources.img_withmo
import org.example.project.model.DevelopmentType
import org.example.project.model.Work

data object WorkSet {
    val works = listOf(
        Work(
            image = Res.drawable.img_withmo,
            name = "withmo",
            logo = null,
            developmentType = DevelopmentType.Team,
            description = """
                3Dフィギュア × ランチャーをコンセプトに、自分の好きな3Dモデルをホーム画面に設定することが出来るランチャーアプリです！
                技育展2024でゆめみ賞を受賞しました。
            """.trimIndent(),
            movieUrl = "https://youtu.be/LpHIyaCpmzE",
            slideUrl = "https://docs.google.com/presentation/d/1P8pJtk8YnazgKQcsffUqSHlIYSe43K1r/edit?usp=drive_link&ouid=105510640231488358645&rtpof=true&sd=true",
            githubUrl = null,
            googlePlayUrl = null,
        ),
        Work(
            image = Res.drawable.img_poitto,
            name = "Poitto",
            logo = null,
            developmentType = DevelopmentType.Team,
            description = """
                イライラしたことを、ぽいっと捨て、嫌なことを忘れようというのをコンセプトにしたアプリです。
                P2Hacks2024にて、ゼミのメンバーと作成しました。
            """.trimIndent(),
            movieUrl = "https://drive.google.com/file/d/1TLRWqH4ABML71wcTONtBbaAH0sVuDppf/view?usp=drive_link",
            slideUrl = "https://docs.google.com/presentation/d/1ob71nWrFgM2NUE0g__QHXBpmHz_WZEp15zaPHj3yO60/edit?usp=drive_link",
            githubUrl = null,
            googlePlayUrl = null,
        ),
        Work(
            image = Res.drawable.img_chatvox,
            name = "ChatVox",
            logo = null,
            developmentType = DevelopmentType.Individual,
            description = """
                VoiceVoxキャラと会話や通話することが出来るアプリです。
                コードラボで学んだRetrofitやRoomを使用するためのアプリを作りたいと思い作成しました。
            """.trimIndent(),
            movieUrl = null,
            slideUrl = null,
            githubUrl = "https://github.com/kei-1111/chat-vox",
            googlePlayUrl = null,
        ),
    )
}
