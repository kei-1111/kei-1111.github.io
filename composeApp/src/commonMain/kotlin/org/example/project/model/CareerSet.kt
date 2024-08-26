package org.example.project.model

data object CareerSet {
    val years = listOf(
        CareerYear(
            year = 2021,
            events = listOf(
                "公立はこだて未来大学入学"
            )
        ),
        CareerYear(
            year = 2022,
            events = listOf(
                "学内ハッカソンP2HacksからAndroid開発を始める"
            )
        ),
        CareerYear(
            year = 2023,
            events = listOf(
                "Open Hack UでNeoHelloの開発スタート",
                "NeoHelloで技育展決勝大会出場"
            )
        ),
        CareerYear(
            year = 2024,
            events = listOf(
                "技育CAMP vol14でwithmoを作り優秀賞",
                "技育博 5月でwithmoを発表",
                "ArticleHubとChatVoxを作成",
                "teamLabでインターン"
            )
        ),
    )
}


data class CareerYear(
    val year: Int,
    val events: List<String>
)