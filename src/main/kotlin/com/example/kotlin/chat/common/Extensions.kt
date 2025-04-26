package com.example.kotlin.chat.common

import com.example.kotlin.chat.repository.ContentType
import com.example.kotlin.chat.repository.Message
import com.example.kotlin.chat.service.MessageVM
import com.example.kotlin.chat.service.UserVM
import java.net.URL

fun MessageVM.asDomainObject(contentType: ContentType = ContentType.PLAIN) =
    Message(
        content = content,
        contentType = contentType,
        sent = sent,
        username = user.name,
        userAvatarImageLink = user.avatarImageLink.toString(),
        id = id
    )

fun Message.asViewModel(): MessageVM =
    MessageVM(
        content = content ,
        user = UserVM(username, URL(userAvatarImageLink)),
        sent = sent ,
        id = id
    )

fun List<Message>.asViewModels(): List<MessageVM> =
    map {it.asViewModel()}