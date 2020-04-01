package com.example.library.service

import com.example.library.domain.Book
import com.example.library.domain.Loan
import com.example.library.domain.User
import com.example.library.exception.BookIsNotAvailableException
import com.example.library.exception.UserReachedLoanLimitException
import com.example.library.repository.LoanRepository
import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import java.time.LocalDate

class LoanServiceTest: ShouldSpec() {

    private val loanRepository = mockk<LoanRepository>()

    private val userService = mockk<UserService>()

    private val bookService = mockk<BookService>()

    private val loanService = spyk<LoanService>(
            LoanService(
                loanRepository = loanRepository,
                userService = userService,
                bookService = bookService),
            recordPrivateCalls = true
    )

    private val user = User(
            name = "Bob",
            documentId = "7896521"
    )

    private val book = Book(
            title = "Clean Code",
            author = "Uncle Bob"
    )

    private val loan = Loan(
            user = user,
            book = book,
            issuedDate = LocalDate.now(),
            loanedUntil = LocalDate.now(),
            returnedDate = null
    )

    init {
        should("create loan entity") {
            every { userService.findById(user.id) } returns user
            every { bookService.findById(book.id) } returns book

            val todaysDate = LocalDate.now()
            val result = loanService.createLoanEntity(user.id, book.id)

            assertSoftly {
                result.user shouldBe user
                result.book shouldBe book
                result.issuedDate shouldBe todaysDate
                result.loanedUntil shouldBe todaysDate
                result.returnedDate shouldBe null
            }
        }

        should("create loan") {
            every { userService.canLoanBook(user.id) } returns true
            every { bookService.isAvailable(book.id) } returns true
            every { loanService["createLoanEntity"](user.id, book.id) } returns loan
            every { loanRepository.save(loan) } returns loan

            val result = loanService.create(user.id, book.id)

            result shouldBe loan
        }

        should("not create loan because of limit of loans") {
            every { userService.canLoanBook(user.id) } returns false

            shouldThrow<UserReachedLoanLimitException> { loanService.create(user.id, book.id) }
        }

        should("not loan book because it is already borrowed") {
            every { userService.canLoanBook(user.id) } returns true
            every { bookService.isAvailable(book.id) } returns false

            shouldThrow<BookIsNotAvailableException> { loanService.create(user.id, book.id) }
        }
    }
}