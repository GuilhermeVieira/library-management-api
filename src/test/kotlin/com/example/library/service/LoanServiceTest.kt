package com.example.library.service

import com.example.library.domain.*
import com.example.library.exception.BookIsNotAvailableException
import com.example.library.exception.BookIsNotBorrowedException
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
        should("get book loans") {
            every { bookService.findById(book.id) } returns book

            val result = loanService.findBookLoans(book.id)

            result shouldBe book.loans
        }

        should("return the current loan") {
            every { loanService.findBookLoans(book.id) } returns mutableListOf(loan)

            val result = loanService.findBookCurrentLoan(book.id)

            result shouldBe loan
        }

        should("return null because book is not currently loaned") {
            every { loanService.findBookLoans(book.id) } returns mutableListOf()

            val result = loanService.findBookCurrentLoan(book.id)

            result shouldBe null
        }

        should("return true because book is available") {
            every { loanService.findBookCurrentLoan(book.id) } returns null

            val result = loanService.isBookAvailable(book.id)

            result shouldBe true
        }

        should("return false because book is currently borrowed") {
            every { loanService.findBookCurrentLoan(book.id) } returns loan

            val result = loanService.isBookAvailable(book.id)

            result shouldBe false
        }

        should("create loan entity") {
            every { userService.findById(user.id) } returns user
            every { bookService.findById(book.id) } returns book

            val todaysDate = LocalDate.now()
            val result = loanService.createLoanEntity(user.id, book.id)

            assertSoftly {
                result.user shouldBe user
                result.book shouldBe book
                result.issuedDate shouldBe todaysDate
                result.loanedUntil shouldBe todaysDate.plusDays(loanService.loanPeriod.toLong())
                result.returnedDate shouldBe null
            }
        }

        should("create loan") {
            every { userService.canLoanBook(user.id) } returns true
            every { loanService["isBookAvailable"](book.id) } returns true
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
            every { loanService["isBookAvailable"](book.id) } returns false

            shouldThrow<BookIsNotAvailableException> { loanService.create(user.id, book.id) }
        }

        should("return book with no fine") {
            every { loanService.findBookCurrentLoan(book.id) } returns loan
            every { loanRepository.save(loan) } returns loan

            val result = loanService.returnBook(book.id)

            result.returnedDate shouldBe LocalDate.now()
            result.fine.fineValue shouldBe 0.0
            result.fine.fineStatus shouldBe FineStatus.NOT_CHARGED
        }

        should("not return book because it is already returned") {
            every { loanService.findBookCurrentLoan(book.id) } returns null

            shouldThrow<BookIsNotBorrowedException> { loanService.returnBook(book.id) }
        }

        should("return book with fine") {
            val fine = Fine()
            fine.fineValue = 2.0
            fine.fineStatus = FineStatus.OPENED
            every { loanService.findBookCurrentLoan(book.id) } returns loan
            every { loanService.computeFine(loan) } returns fine
            every { loanRepository.save(loan) } returns loan

            val result = loanService.returnBook(book.id)

            result.returnedDate shouldBe LocalDate.now()
            result.fine.fineValue shouldBe 2.0
            result.fine.fineStatus shouldBe FineStatus.OPENED
        }

        should("return 0.0 as fine because user delivered book before the due date") {
            val onTimeLoan = mockk<Loan>()
            every { onTimeLoan.loanedUntil } returns LocalDate.now().plusDays(1)

            val result = loanService.computeFine(onTimeLoan)

            result.fineValue shouldBe 0.0
            result.fineStatus shouldBe FineStatus.NOT_CHARGED
        }

        should("return 0.0 as fine because user delivered book in the loanedUntil date") {
            val onTimeLoan = mockk<Loan>()
            every { onTimeLoan.loanedUntil } returns LocalDate.now()

            val result = loanService.computeFine(onTimeLoan)

            result.fineValue shouldBe 0.0
            result.fineStatus shouldBe FineStatus.NOT_CHARGED
        }

        should("return a fine because user delayed one day") {
            val onTimeLoan = mockk<Loan>()

            for (delayedDays in 1..60) {
                every { onTimeLoan.loanedUntil } returns LocalDate.now().minusDays(delayedDays.toLong())

                val result = loanService.computeFine(onTimeLoan)

                result.fineValue shouldBe delayedDays * loanService.finePerDay
                result.fineStatus shouldBe FineStatus.OPENED
            }
        }
    }
}