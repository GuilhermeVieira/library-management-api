package com.example.library.rest

import com.example.library.repository.BookRepository
import com.example.library.repository.LoanRepository
import com.example.library.repository.UserRepository
import com.example.library.rest.exchange.BookExchange
import com.example.library.rest.exchange.LoanExchange
import com.example.library.rest.exchange.UserExchange
import com.example.library.service.BookService
import com.example.library.service.LoanService
import com.example.library.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

const val USER_NAME = "Bob"
const val USER_DOCUMENT_ID = "1894226"
const val BOOK_TITLE = "Clean Code"
const val BOOK_AUTHOR = "Uncle Bob"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
abstract class BaseControllerTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var loanService: LoanService

    @Autowired
    lateinit var loanRepository: LoanRepository

    fun buildUserRequest(name: String = USER_NAME, id: String = USER_DOCUMENT_ID): UserExchange {
        return UserExchange(
                name = name,
                documentId = id
        )
    }

    fun buildBookRequest(title: String = BOOK_TITLE, author: String = BOOK_AUTHOR): BookExchange {
        return BookExchange(
                title = title,
                author = author
        )
    }

    fun buildLoanRequest(userId: String, bookId: String): LoanExchange {
        return LoanExchange(
                userId = userId,
                bookId = bookId
        )
    }

    fun createUser(id: String): UserExchange {
        return objectMapper.readValue(mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildUserRequest(id = id))))
                .andReturn().response.contentAsString, UserExchange::class.java)
    }

    fun createBook(): BookExchange {
        return objectMapper.readValue(mockMvc.perform(post("/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildBookRequest())))
                .andReturn().response.contentAsString, BookExchange::class.java)
    }

    fun createLoan(userId: String, bookId: String): LoanExchange {
        return objectMapper.readValue(mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(userId, bookId))))
                .andReturn().response.contentAsString, LoanExchange::class.java)
    }

    fun returnBook(bookId: String) {
        mockMvc.perform(post("/loans/${bookId}")
                .contentType(MediaType.APPLICATION_JSON))
    }

}