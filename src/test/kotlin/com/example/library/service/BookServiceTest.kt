package com.example.library.service

import com.example.library.domain.Book
import com.example.library.exception.BookNotFoundException
import com.example.library.repository.BookRepository
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull

class BookServiceTest: ShouldSpec() {

    private val bookRepository = mockk<BookRepository>()

    private val bookService = BookService(bookRepository)

    private fun buildBook(available: Boolean = true) =
            Book(
                    title = "Clean Code",
                    author = "Uncle Bob",
                    available = available
            )

    init {
        should("create book") {
            val book = buildBook()
            every { bookRepository.save(book) } returns book

            val result = bookService.create(book)

            result shouldBe book

        }

        should("find book if it exists") {
            val book = buildBook()
            every { bookRepository.findByIdOrNull(book.id) } returns book

            val result = bookService.findById(book.id)

            result shouldBe book
        }

        should("not find book if it does not exist") {
            val book = buildBook()
            every { bookRepository.findByIdOrNull(book.id) } returns null

            shouldThrow<BookNotFoundException> { bookService.findById(book.id) }
        }

        should("return true because book is not currently borrowed") {
            val book = buildBook()
            every { bookService.findById(book.id) } returns book

            val result = bookService.isAvailable(book.id)

            result shouldBe true
        }

        should("return false because book is currently borrowed") {
            val book = buildBook(false)
            every { bookService.findById(book.id) } returns book

            val result = bookService.isAvailable(book.id)

            result shouldBe false
        }
    }
}