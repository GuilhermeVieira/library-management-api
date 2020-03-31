package com.example.library.rest.exchange

import com.example.library.domain.User
import com.fasterxml.jackson.annotation.JsonProperty

data class UserExchange(
        var id: String? = null,
        val name: String,
        @JsonProperty("document_id")
        val documentId: String
)

fun UserExchange.toDomain(): User {
        return User(name = name, documentId = documentId)
}

fun User.toUserExchange(): UserExchange {
        return UserExchange(id = id, name = name, documentId = documentId)
}