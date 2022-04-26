package io.github.grebenindmitry.babilado.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class WSMessage

@Serializable
@SerialName("newMessage")
data class WSNewMessage(val newMessage: Message) : WSMessage()