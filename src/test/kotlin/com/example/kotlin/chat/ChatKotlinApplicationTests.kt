package com.example.kotlin.chat

import app.cash.turbine.test
import com.example.kotlin.chat.repository.ContentType
import com.example.kotlin.chat.repository.Message
import com.example.kotlin.chat.repository.MessageRepository
import com.example.kotlin.chat.service.MessageVM
import com.example.kotlin.chat.service.UserVM
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb"
    ]
)
class ChatKotlinApplicationTests (
    @Autowired val rsocketBuilder: RSocketRequester.Builder,
    @Autowired val messageRepository: MessageRepository,
    @LocalServerPort val serverPort: Int
) {

    private final val now = Instant.now()
    private final val secondBeforeNow: Instant = now.minusSeconds(1)
    private final val twoSecondsBeforeNow: Instant = now.minusSeconds(2)
    private final val messageList = listOf(
        Message("testMessage1", ContentType.PLAIN, twoSecondsBeforeNow, "test", "http://test.com"),
        Message("testMessage2", ContentType.MARKDOWN, secondBeforeNow, "test", "http://test.com"),
    )

    @BeforeEach
    fun setup() {

        runBlocking {
            val savedMessages = messageRepository.saveAll(messageList)
        }

    }

    @AfterEach
    fun teardown() {
        runBlocking {
            messageRepository.deleteAll()
        }
    }

    @Test
    fun contextLoads() {

    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun `test that messages API streams latest messages`() {
        runBlocking {
            val rSocketRequester =
                rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

            rSocketRequester
                .route("api.v1.messages.stream")
                .retrieveFlow<MessageVM>()
                .test {


                    expectNoEvents()

                    launch {
                        rSocketRequester.route("api.v1.messages.stream")
                            .dataWithType(flow {
                                emit(
                                    MessageVM(
                                        "`HelloWorld`",
                                        UserVM("test", URL("http://test.com")),
                                        now.plusSeconds(1)
                                    )
                                )
                            })
                            .retrieveFlow<Void>()
                            .collect()
                    }

                    assertThat(expectItem().forTesting())
                        .isEqualTo(
                            MessageVM(
                                "<body><p><code>HelloWorld</code></p></body>",
                                UserVM("test", URL("http://test.com")),
                                now.plusSeconds(1).truncatedTo(ChronoUnit.MILLIS)
                            )
                        )

                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @ExperimentalTime
    @Test
    fun `test that messages streamed to the API is stored`() {
        runBlocking {
            launch {
                val rSocketRequester =
                    rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

                rSocketRequester.route("api.v1.messages.stream")
                    .dataWithType(flow {
                        emit(
                            MessageVM(
                                "`HelloWorld`",
                                UserVM("test", URL("http://test.com")),
                                now.plusSeconds(1)
                            )
                        )
                    })
                    .retrieveFlow<Void>()
                    .collect()
            }

            delay(2.seconds)

            messageRepository.findAll()
                .first { it.content.contains("HelloWorld") }
                .apply {
                    assertThat(this.forTesting())
                        .isEqualTo(
                            Message(
                                "`HelloWorld`",
                                ContentType.PLAIN,
                                now.plusSeconds(1).truncatedTo(ChronoUnit.MILLIS),
                                "test",
                                "http://test.com"
                            )
                        )
                }
        }
    }

}
