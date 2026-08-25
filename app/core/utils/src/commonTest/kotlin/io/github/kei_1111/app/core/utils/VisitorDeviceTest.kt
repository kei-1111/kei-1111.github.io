package io.github.kei_1111.app.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class VisitorDeviceTest {

    @Test
    fun labelsChromeOnMacWithMajorVersion() {
        val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/131.0.0.0 Safari/537.36"

        assertEquals("Chrome 131 (macOS) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun detectsEdgeBeforeChrome() {
        val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.86"

        assertEquals("Edge 131 (Windows) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun detectsOperaBeforeChrome() {
        val ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/131.0.0.0 Safari/537.36 OPR/116.0.0.0"

        assertEquals("Opera 116 (Linux) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun detectsSamsungInternetBeforeChrome() {
        val ua = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "SamsungBrowser/27.0 Chrome/125.0.0.0 Mobile Safari/537.36"

        assertEquals("Samsung Internet 27 (SM-S918B) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun detectsSafariOnlyWithVersionToken() {
        val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
            "Version/17.4 Safari/605.1.15"

        assertEquals("Safari 17 (macOS) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun extractsTheAndroidModelAndDropsTheBuildSuffix() {
        val ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/AD1A.240418.003) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        assertEquals("Chrome 124 (Pixel 8) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun fallsBackToAndroidWhenTheModelIsMissing() {
        val ua = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Mobile Safari/537.36"

        assertEquals("Chrome 124 (Android) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun labelsIpadAsIpadOs() {
        val ua = "Mozilla/5.0 (iPad; CPU OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) " +
            "Version/17.4 Mobile/15E148 Safari/604.1"

        assertEquals("Safari 17 (iPadOS) wasmJs", visitorDeviceLabel(ua))
    }

    @Test
    fun fallsBackToGenericLabelForUnknownUserAgent() {
        assertEquals("Web Browser wasmJs", visitorDeviceLabel("SomethingElse/1.0"))
    }
}
