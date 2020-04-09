package com.example.library.rest

import com.example.library.domain.Book
import com.example.library.domain.Loan
import com.example.library.domain.User
import com.example.library.repository.BookRepository
import com.example.library.repository.LoanRepository
import com.example.library.repository.UserRepository
import com.example.library.rest.exchange.BookExchange
import com.example.library.rest.exchange.LoanExchange
import com.example.library.rest.exchange.UserExchange
import com.example.library.rest.exchange.toDomain
import com.example.library.service.BookService
import com.example.library.service.LoanService
import com.example.library.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.time.LocalDate
import java.util.*

const val USER_NAME = "Bob"
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

    fun buildUserRequest(name: String = USER_NAME, id: String = UUID.randomUUID().toString()): UserExchange {
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

    fun createBaseLoan(user: User,
                       book: Book,
                       issuedDate: LocalDate = LocalDate.now(),
                       dueDate: LocalDate = issuedDate.plusDays(loanService.loanPeriod.toLong()),
                       returnedDate: LocalDate? = null) =
            Loan(
                    user = user,
                    book = book,
                    issuedDate = issuedDate,
                    dueDate = dueDate,
                    returnedDate = returnedDate
            )

    fun generateCreatedBook() = saveBook(buildBookRequest().toDomain())

    fun generateCreatedUser() = saveUser(buildUserRequest().toDomain())

    fun User.generateLoans(n: Int = 1) =  repeat(n) {
        val book = generateCreatedBook()
        saveLoan(createBaseLoan(this, book))
    }

    fun saveUser(user: User) = userRepository.save(user)

    fun saveBook(book: Book) = bookRepository.save(book)

    fun saveLoan(loan: Loan) = loanRepository.save(loan)

    fun returnBook(bookId: String) {
        mockMvc.perform(post("/loans/${bookId}")
                .contentType(MediaType.APPLICATION_JSON))
    }

}