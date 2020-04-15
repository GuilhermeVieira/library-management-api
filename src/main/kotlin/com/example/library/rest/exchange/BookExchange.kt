package com.example.library.rest.exchange

import com.example.library.domain.Book

data class BookExchange(
        var id: String? = null,
        val title: String,
        val author: String
)

fun BookExchange.toDomain() = Book(title = title, author = author)

fun Book.toBookExchange() = BookExchange(id = id, title = title, author = author)