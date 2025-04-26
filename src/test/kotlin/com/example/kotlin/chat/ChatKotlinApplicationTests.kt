package com.example.kotlin.chat

import com.example.kotlin.chat.common.asDomainObject
import com.example.kotlin.chat.common.asViewModel
import com.example.kotlin.chat.repository.ContentType
import com.example.kotlin.chat.repository.Message
import com.example.kotlin.chat.repository.MessageRepository
import com.example.kotlin.chat.service.MessageVM
import com.example.kotlin.chat.service.UserVM
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import java.net.URI
import java.net.URL
import java.time.Instant

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb"
    ]
)
class ChatKotlinApplicationTests {

    @Autowired
    lateinit var client: TestRestTemplate

    @Autowired
    lateinit var messageRepository: MessageRepository

    lateinit var lastMessageId: String

    val now: Instant = Instant.now()


    @Test
    fun contextLoads() {
    }

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

            lastMessageId = savedMessages.first().id ?: ""
        }

    }

    @AfterEach
    fun teardown() {
        runBlocking {
            messageRepository.deleteAll()
        }
    }


    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that messages API returns latest messages`(withLastMessageId: Boolean) {
        runBlocking {
            val messages: List<MessageVM>? = client.exchange(
                RequestEntity<Any>(
                    HttpMethod.GET,
                    URI("/api/v1/messages?lastMessageId=${if (withLastMessageId) lastMessageId else ""}"),
                ),
                object : ParameterizedTypeReference<List<MessageVM>>() {}
            ).body

            if (!withLastMessageId) {
                assertThat(messages?.map { it.forTesting() })
                    .first().isEqualTo(messageList.first().asViewModel().forTesting())
            }

            assertThat(messages?.map { it.forTesting() })
                .containsSubsequence(messageList.last().asViewModel().forTesting())
        }
    }

    @Test
    fun `test that messages posted to the api is stored`() {
        runBlocking {
            val messageVM = MessageVM(
                content = "HelloWorld",
                user = UserVM("user", URL("http://test.com")),
                sent = now.plusSeconds(1)
            )
            client.postForEntity<Any>(
                URI("/api/v1/messages"),
                messageVM.forTesting()

            )

            messageRepository.findAll()
                .first { it.content.contains("HelloWorld") }
                .apply {
                    assertThat(this.forTesting())
                        .isEqualTo(messageVM.asDomainObject().forTesting())
                }
        }
    }

}
