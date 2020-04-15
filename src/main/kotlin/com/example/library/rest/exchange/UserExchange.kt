package com.example.library.rest.exchange

import com.example.library.domain.User

data class UserExchange(
        var id: String? = null,
        val name: String,
        val documentId: String
)

fun UserExchange.toDomain() = User(name = name, documentId = documentId)

fun User.toUserExchange() = UserExchange(id = id, name = name, documentId = documentId)