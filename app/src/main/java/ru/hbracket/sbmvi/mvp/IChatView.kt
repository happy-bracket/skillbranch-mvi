package ru.hbracket.sbmvi.mvp

import ru.hbracket.sbmvi.domain.Message

interface IChatView {

    fun setLoading(loading: Boolean)

    fun addMessages(messages: List<Message>)

    fun showError(err: Throwable)

}