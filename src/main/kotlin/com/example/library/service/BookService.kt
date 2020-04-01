package com.example.library.service

import com.example.library.domain.Book
import com.example.library.exception.BookNotFoundException
import com.example.library.repository.BookRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BookService(val bookRepository: BookRepository) {

    fun create(book: Book) = bookRepository.save(book)

    fun findById(id: String) = bookRepository.findByIdOrNull(id)
            ?: throw BookNotFoundException()

    fun isAvailable(id: String) = findById(id).available

}