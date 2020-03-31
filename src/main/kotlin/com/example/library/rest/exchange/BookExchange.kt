package com.example.library.rest.exchange

import com.example.library.domain.Book

data class BookExchange(
        var id: String? = null,
        val title: String,
        val author: String
)

fun BookExchange.toDomain(): Book {
    return Book(title = title, author = author)
}

fun Book.toBookExchange(): BookExchange {
    return BookExchange(id = id, title = title, author = author)
}