package io.github.kei_1111.data

import io.github.kei_1111.model.SNS
import kei_1111.composeapp.generated.resources.Res
import kei_1111.composeapp.generated.resources.img_github
import kei_1111.composeapp.generated.resources.img_note
import kei_1111.composeapp.generated.resources.img_x

data object SNSSet {
    val SNSs = listOf(
        SNS(
            image = Res.drawable.img_x,
            name = "X",
            url = "https://x.com/kei_1111_",
        ),
        SNS(
            image = Res.drawable.img_github,
            name = "GitHub",
            url = "https://github.com/kei-1111",
        ),
        SNS(
            image = Res.drawable.img_note,
            name = "note",
            url = "https://note.com/kei_1111_",
        ),
    )
}
