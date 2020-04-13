package com.example.library.service

import com.example.library.domain.*
import com.example.library.exception.*
import com.example.library.repository.LoanRepository
import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

class LoanServiceTest: ShouldSpec() {

    private val loanRepository = mockk<LoanRepository>()

    private val userService = mockk<UserService>()

    private val bookService = mockk<BookService>()

    private val loanService = spyk<LoanService>(
            LoanService(
                loanRepository = loanRepository,
                userService = userService,
                bookService = bookService)
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
            dueDate = LocalDate.now(),
            returnedDate = null
    )

    init {

        should("find loan") {
            every { loanRepository.findByIdOrNull(loan.id) } returns loan

            val result = loanService.findById(loan.id)

            result shouldBe loan
        }

        should("not find loan") {
            every { loanRepository.findByIdOrNull(loan.id) } returns null

            shouldThrow<LoanNotFoundException> { loanService.findById(loan.id) }
        }

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
                result.dueDate shouldBe todaysDate.plusDays(loanService.loanPeriod.toLong())
                result.returnedDate shouldBe null
            }
        }

        should("create loan") {
            every { userService.canLoanBook(user.id) } returns true
            every { loanService.isBookAvailable(book.id) } returns true
            every { loanService.createLoanEntity(user.id, book.id) } returns loan
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
            every { loanService.isBookAvailable(book.id) } returns false

            shouldThrow<BookIsNotAvailableException> { loanService.create(user.id, book.id) }
        }

        should("return book") {
            every { loanService.findBookCurrentLoan(book.id) } returns loan
            every { loanService.close(loan) } returns loan

            val result = loanService.returnBook(book.id)

            result shouldBe loan
        }

        should("not return book because it is already returned") {
            every { loanService.findBookCurrentLoan(book.id) } returns null

            shouldThrow<BookIsNotBorrowedException> { loanService.returnBook(book.id) }
        }


        should("return null as fine because user delivered book before the due date") {
            val onTimeLoan = spyk<Loan>()
            every { onTimeLoan.dueDate } returns LocalDate.now().plusDays(1)
            every { loanRepository.save(onTimeLoan) } returns onTimeLoan

            val result = loanService.close(onTimeLoan)

            result.returnedDate shouldBe LocalDate.now()
            result.fine shouldBe null
        }

        should("return null as fine because user delivered book in the loanedUntil date") {
            val onTimeLoan = spyk<Loan>()
            every { onTimeLoan.dueDate } returns LocalDate.now()
            every { loanRepository.save(onTimeLoan) } returns onTimeLoan

            val result = loanService.close(onTimeLoan)

            result.returnedDate shouldBe LocalDate.now()
            result.fine shouldBe null
        }

        should("return a fine because user delayed one day") {
            val overdueLoan = spyk<Loan>()

            for (delayedDays in 1..60) {
                every { overdueLoan.dueDate } returns LocalDate.now().minusDays(delayedDays.toLong())
                every { loanRepository.save(overdueLoan) } returns overdueLoan

                val result = loanService.close(overdueLoan)

                result.returnedDate shouldBe LocalDate.now()
                result.fine?.value shouldBe delayedDays * loanService.finePerDay
                result.fine?.status shouldBe FineStatus.OPENED
            }
        }

        should("return user loans") {
            mockkObject(user)
            every { userService.findById(user.id) } returns user
            every { user.loans } returns mutableListOf(loan)

            val result = loanService.findUserLoans(user.id)

            result shouldBe mutableListOf(loan)
        }

        should("pay fine") {
            val overdueLoan = spyk<Loan>()
            every { overdueLoan.id } returns loan.id
            every { loanService.findById(loan.id) } returns overdueLoan
            every { overdueLoan.returnedDate } returns LocalDate.now()
            every { overdueLoan.fine } returns Fine(4.0, FineStatus.OPENED)
            every { loanRepository.save(overdueLoan) } returns overdueLoan

            val result = loanService.payFine(overdueLoan.id)

            result.fine?.status shouldBe FineStatus.PAID
        }

        should("not pay fine because loan was not closed yet") {
            every { loanService.findById(loan.id) } returns loan

            shouldThrow<CouldNotPayFineException> { loanService.payFine(loan.id) }
        }

        should("not pay fine because loan has no fine") {
            val overdueLoan = spyk<Loan>()
            every { loanService.findById(loan.id) } returns overdueLoan
            every { overdueLoan.returnedDate } returns LocalDate.now()
            every { overdueLoan.fine } returns null

            shouldThrow<CouldNotPayFineException> { loanService.payFine(loan.id) }
        }

        should("not pay fine because loan fine is already paid") {
            val overdueLoan = spyk<Loan>()
            every { loanService.findById(loan.id) } returns overdueLoan
            every { overdueLoan.returnedDate } returns LocalDate.now()
            every { overdueLoan.fine } returns Fine(4.0, FineStatus.PAID)

            shouldThrow<CouldNotPayFineException> { loanService.payFine(loan.id) }
        }
    }
}