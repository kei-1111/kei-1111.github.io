@file:Suppress("MaxLineLength")

package io.github.kei_1111.core.data

import io.github.kei_1111.core.model.DevelopmentType
import io.github.kei_1111.core.model.Work
import kei_1111.core.data.generated.resources.Res
import kei_1111.core.data.generated.resources.img_articlehub
import kei_1111.core.data.generated.resources.img_buswift
import kei_1111.core.data.generated.resources.img_chatvox
import kei_1111.core.data.generated.resources.img_neohello
import kei_1111.core.data.generated.resources.img_poitto
import kei_1111.core.data.generated.resources.img_withmo

data object WorkSet {
    val works = listOf(
        Work(
            image = Res.drawable.img_neohello,
            name = "NeoHello",
            logo = null,
            developmentType = DevelopmentType.Team,
            description = """
                SNSの交換ってめんどくさくないですか？種類が多いから、Instagramを交換して〜、Twitterを交換して〜、ってなりますよね。NeoHelloは、そんなSNS交換の煩わしさを軽減するためのアプリです。
                Near by Connection APIを用いて、近くにいる人と簡単にSNSを交換することが出来ます。
                Open Hack U 2023にて、友人とともに作成しました。その後、技育展2023決勝大会にも出展しました。
            """.trimIndent(),
            movieUrl = null,
            slideUrl = "https://docs.google.com/presentation/d/1ur-FCswotPiY6VaIgWp6wYH-epDrRuLU/edit#slide=id.p1",
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
        Work(
            image = Res.drawable.img_articlehub,
            name = "ArticleHub",
            logo = null,
            developmentType = DevelopmentType.Individual,
            description = """
                Qiitaや、Zenn、noteから最新記事を取得し、一覧で見ることが出来るアプリです。お気に入り機能もあり、よかった記事を保存することが出来ます。
                エンジニアにとって、情報収集することは大事だと思い、けど色々サイトを見て回るのはめんどくさく、一括で見ることが出来るArticleHubを作成しました。
            """.trimIndent(),
            movieUrl = null,
            slideUrl = null,
            githubUrl = "https://github.com/kei-1111/article-hub",
            googlePlayUrl = null,
        ),
        Work(
            image = Res.drawable.img_withmo,
            name = "withmo",
            logo = null,
            developmentType = DevelopmentType.Team,
            description = """
                推しをホーム画面に配置したいと思いませんか？withmoは、3Dフィギュア × ランチャーをコンセプトに、自分の好きな3Dモデルをホーム画面に設定することが出来るランチャーアプリです！
                技育CAMP2023 vol14にて作成し、優秀賞を受賞しました。その後、技育展2024に出展し、ゆめみ賞を受賞しました。
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
                イライラしたことを、宇宙にぽいっと捨て、嫌なことを忘れようというのをコンセプトにしたアプリです。よりスッキリ出来るように、捨てる際のアニメーションに力を入れました。
                また、捨てたイライラは、宇宙を漂うゴミとして、他のユーザが見ることが出来ます。他の人のイライラを見て逆に気持ちが落ち着くかもと思いこの機能を作成しました。
                P2Hacks2024にて、ゼミのメンバーと作成しました。
            """.trimIndent(),
            movieUrl = "https://drive.google.com/file/d/1TLRWqH4ABML71wcTONtBbaAH0sVuDppf/view?usp=drive_link",
            slideUrl = "https://docs.google.com/presentation/d/1ob71nWrFgM2NUE0g__QHXBpmHz_WZEp15zaPHj3yO60/edit?usp=drive_link",
            githubUrl = "https://github.com/kei-1111/poitto",
            googlePlayUrl = null,
        ),
        Work(
            image = Res.drawable.img_buswift,
            name = "ばすうぃふと",
            logo = null,
            developmentType = DevelopmentType.Team,
            description = """
                函館バスの時刻検索を行うアプリです。高度ICT演習にて作成しています。
                もともとは、名前の通り、Swiftを用いてのiOSアプリでしたが、2024年度からAndroid版の開発を始めて自分がAndroidリーダーとして活動しています。
            """.trimIndent(),
            movieUrl = null,
            slideUrl = null,
            githubUrl = null,
            googlePlayUrl = null,
        ),
    )
}
