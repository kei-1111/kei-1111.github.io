package io.github.kei_1111.server.plugins

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val UNHANDLED_EXCEPTION_MESSAGE = "Unhandled exception"

/** ルート例外の記録だけを観測する。キャンセルは再スローされるため HTTP ステータスでは通常失敗と区別できない。 */
private fun capturingErrorLogs(block: () -> Unit): List<String> {
    val appender = ListAppender<ILoggingEvent>()
    val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    appender.start()
    root.addAppender(appender)
    try {
        block()
    } finally {
        root.detachAppender(appender)
        appender.stop()
    }
    return appender.list.map { it.formattedMessage }
}

class StatusPagesTest {

    @Test
    fun convertsUnhandledExceptionsInto500() = testApplication {
        application {
            configureStatusPages()
            routing {
                get("/boom") { error("boom") }
            }
        }

        assertEquals(HttpStatusCode.InternalServerError, client.get("/boom").status)
    }

    @Test
    fun leavesNormalResponsesUntouched() = testApplication {
        application {
            configureStatusPages()
            routing {
                get("/ok") { call.respondText("ok") }
            }
        }

        assertEquals(HttpStatusCode.OK, client.get("/ok").status)
    }

    @Test
    fun logsUnhandledExceptionsAsServerErrors() {
        val logs = capturingErrorLogs {
            testApplication {
                application {
                    configureStatusPages()
                    routing {
                        get("/boom") { error("boom") }
                    }
                }

                client.get("/boom")
            }
        }

        assertTrue(logs.any { it == UNHANDLED_EXCEPTION_MESSAGE })
    }

    @Test
    fun rethrowsCancellationWithoutLoggingItAsAServerError() {
        val logs = capturingErrorLogs {
            testApplication {
                application {
                    configureStatusPages()
                    routing {
                        get("/cancel") { throw CancellationException("cancelled") }
                    }
                }

                client.get("/cancel")
            }
        }

        assertFalse(logs.any { it == UNHANDLED_EXCEPTION_MESSAGE })
    }
}
