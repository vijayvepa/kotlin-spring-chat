package com.example.kotlin.chat

import com.example.kotlin.chat.repository.Message
import com.example.kotlin.chat.service.MessageVM
import java.time.temporal.ChronoUnit

fun MessageVM.forTesting() =
    copy(id = null, sent = sent.truncatedTo(ChronoUnit.MILLIS))

fun Message.forTesting() =
    copy(id= null, sent =  sent.truncatedTo(ChronoUnit.MILLIS))