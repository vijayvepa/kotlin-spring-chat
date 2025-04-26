package com.example.kotlin.chat.service

import com.github.javafaker.Faker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant
import kotlin.random.Random

@Service
class FakeMessageService : MessageService {



    val users: Map<String, UserVM> = mapOf(
        "Shakespeare"  to UserVM("Shakespeare", URL("https://blog.12min.com/wp-content/uploads/2018/05/27d-William-Shakespeare.jpg")),
        "RickAndMorty" to UserVM("RickAndMorty", URL("https://a.media-amazon.com/images/I/51JEhqmBwyL._SY445_SX342_.jpg")),
        "Yoda"         to UserVM("Yoda", URL("https://news.toyark.com/wp-content/uploads/sites/4/2019/03/SH-Figuarts-Yoda-001.jpg"))
    )

    val usersQuotes: Map<String, () -> String> = mapOf(
        "Shakespeare"  to { Faker.instance().shakespeare().asYouLikeItQuote() },
        "RickAndMorty" to { Faker.instance().rickAndMorty().quote() },
        "Yoda"         to { Faker.instance().yoda().quote() }
    )

    override fun latest(): Flow<MessageVM> {
        val count = Random.nextInt(1, 15)
        val list= (0..count).map {
            val user = users.values.random()
            val userQuote = usersQuotes.getValue(user.name).invoke()

            MessageVM(userQuote, user, Instant.now(), Random.nextBytes(10).toString())
        }.toList()
        return flow {
            for (item in list) {emit(item)}
        }
    }

    override fun after(messageId: String): Flow<MessageVM> {
        return latest()
    }

    override suspend fun post(messages: Flow<MessageVM>) {
        TODO("Not yet implemented")
    }

    override fun stream(): Flow<MessageVM> {
        return latest()
    }
}
