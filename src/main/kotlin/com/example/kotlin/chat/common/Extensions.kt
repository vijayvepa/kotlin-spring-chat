package com.example.kotlin.chat.common

import com.example.kotlin.chat.repository.ContentType
import com.example.kotlin.chat.repository.Message
import com.example.kotlin.chat.service.MessageVM
import com.example.kotlin.chat.service.UserVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
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
        content = contentType.render(content),
        user = UserVM(username, URL(userAvatarImageLink)),
        sent = sent,
        id = id
    )

fun Flow<Message>.asViewModels(): Flow<MessageVM> =
    map { it.asViewModel() }

fun ContentType.render(content: String): String =
    when (this) {
        ContentType.PLAIN -> content
        ContentType.MARKDOWN -> {
            val flavor = CommonMarkFlavourDescriptor()
            HtmlGenerator(
                markdownText = content,
                root = MarkdownParser(flavor).buildMarkdownTreeFromString(content),
                flavour = flavor
            ).generateHtml()
        }
    }

