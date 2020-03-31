package com.example.library.rest

import com.example.library.rest.exchange.BookExchange
import com.example.library.rest.exchange.toBookExchange
import com.example.library.rest.exchange.toDomain
import com.example.library.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/books")
@RestController
class BookController(val bookService: BookService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody book: BookExchange): BookExchange {
        return bookService.create(book.toDomain()).toBookExchange()
    }

    @GetMapping("/{id}")
    fun findBook(@PathVariable id: String): BookExchange {
        return bookService.findById(id).toBookExchange()
    }

}