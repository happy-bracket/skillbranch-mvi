package ru.hbracket.sbmvi.domain

data class Message(
    val author: String,
    val content: String,
    val createdAt: Long
)