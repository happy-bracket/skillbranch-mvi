package ru.hbracket.sbmvi.mvi

import ru.hbracket.sbmvi.domain.ChatDataSource
import ru.hbracket.sbmvi.domain.Message
import java.io.Serializable

data class ChatState(
    val username: String,
    val input: String,
    val messages: Messages
) : Serializable

sealed class Messages {

    object Loading : Messages()
    object Error : Messages()
    data class Value(val msgs: List<Message>) : Messages()

}

sealed class ChatMutation {

    object SendMessage : ChatMutation()
    object ConnectionFailed : ChatMutation()
    data class NewInput(val newInput: String) : ChatMutation()
    data class NewMessages(val messages: List<Message>) : ChatMutation()

}

sealed class ChatEffect {

    object SubToChat : ChatEffect()
    object LoadHistory : ChatEffect()
    data class SendMessage(val from: String, val content: String) : ChatEffect()

}


infix fun ChatState.update(mutation: ChatMutation): Pair<ChatState, Set<ChatEffect>> {
    return when (mutation) {
        is ChatMutation.SendMessage ->
            copy(input = "") to setOf(ChatEffect.SendMessage(username, input))
        is ChatMutation.ConnectionFailed ->
            copy(messages = Messages.Error) to emptySet()
        is ChatMutation.NewInput ->
            copy(input = mutation.newInput) to emptySet<ChatEffect>()
        is ChatMutation.NewMessages -> {
            when (messages) {
                is Messages.Loading,
                is Messages.Error ->
                    copy(messages = Messages.Value(mutation.messages)) to emptySet()
                is Messages.Value ->
                    copy(messages = Messages.Value(
                        (messages.msgs + mutation.messages).sortedBy { it.createdAt }
                    )) to emptySet()
            }
        }
    }

}

class ChatHandler : Handler<ChatEffect, ChatMutation> {

    private val dataSource = ChatDataSource()

    override fun handle(effect: ChatEffect, sink: (ChatMutation) -> Unit) {
        when (effect) {
            is ChatEffect.SubToChat ->
                dataSource.messages()
                    .map { ChatMutation.NewMessages(listOf(it)) as ChatMutation }
                    .onErrorReturn { ChatMutation.ConnectionFailed }
                    .subscribe(sink)
            is ChatEffect.SendMessage ->
                dataSource.send(effect.from, effect.content)
                    .subscribe()
        }
    }

}