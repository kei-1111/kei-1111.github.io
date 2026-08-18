package io.github.kei_1111.app.core.api.network

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiConfigTest {

    // 各 ApiImplTest は "$API_BASE_URL/..." と定数を自己参照するため、ローカル検証で
    // localhost へ書き換えた値がコミットに紛れ込んでも検知できない。ここで実値をピンして CI で止める。
    @Test
    fun baseUrlPinsTheProductionOrigin() {
        assertEquals(
            "https://kei-1111-server-672756196519.asia-northeast1.run.app",
            API_BASE_URL,
        )
    }
}
