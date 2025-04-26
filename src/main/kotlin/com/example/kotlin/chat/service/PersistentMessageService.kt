package com.example.kotlin.chat.service

import com.example.kotlin.chat.common.asDomainObject
import com.example.kotlin.chat.common.asViewModel
import com.example.kotlin.chat.common.asViewModels
import com.example.kotlin.chat.repository.ContentType
import com.example.kotlin.chat.repository.Message
import com.example.kotlin.chat.repository.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.net.URL

@Service
@Primary
class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {

    override suspend fun latest(): List<MessageVM> =
        messageRepository.findLatest().asViewModels()

    override suspend fun after(messageId: String): List<MessageVM> =
        messageRepository.findLatest(messageId).asViewModels()

    override suspend fun post(message: MessageVM) {
       messageRepository.save(message.asDomainObject())
    }
}