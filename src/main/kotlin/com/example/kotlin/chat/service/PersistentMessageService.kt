package com.example.kotlin.chat.service

import com.example.kotlin.chat.common.asDomainObject
import com.example.kotlin.chat.common.asViewModels
import com.example.kotlin.chat.common.render
import com.example.kotlin.chat.repository.ContentType
import com.example.kotlin.chat.repository.MessageRepository
import kotlinx.coroutines.flow.*
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {

    /**
     * Broadcast messages to the connected clients.
     */
    val sender: MutableSharedFlow<MessageVM> = MutableSharedFlow()

    override fun latest(): Flow<MessageVM> =
        messageRepository.findLatest().asViewModels()

    override fun after(messageId: String): Flow<MessageVM> =
        messageRepository.findLatest(messageId).asViewModels()

    override fun stream(): Flow<MessageVM> = sender

    override suspend fun post(messages: Flow<MessageVM>) =
        messages
            .onEach { sender.emit(it.asRendered()) } //broadcast before saving
            .map { it.asDomainObject() }
            .let { messageRepository.saveAll(it) }
            .collect()




}

private fun MessageVM.asRendered(contentType: ContentType = ContentType.MARKDOWN): MessageVM =
    this.copy(content = contentType.render(this.content))

