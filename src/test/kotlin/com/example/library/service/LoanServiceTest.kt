package com.example.library.service

import com.example.library.domain.*
import com.example.library.exception.*
import com.example.library.repository.LoanRepository
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

class LoanServiceTest : ShouldSpec() {

    private val loanRepository = mockk<LoanRepository>()

    private val userService = mockk<UserService>()

    private val bookService = mockk<BookService>()

    private val loanService = LoanService(
                loanRepository = loanRepository,
                userService = userService,
                bookService = bookService
    )

    private val user = buildUser()

    private val book = buildBook()

    private val loan = buildLoan()

    init {

        should("create loan") {
            val todaysDate = LocalDate.now()
            every { userService.canLoanBook(user.id) } returns true
            every { bookService.findById(book.id).loans } returns mutableListOf(loan)
            every { bookService.findById(book.id) } returns book
            every { userService.findById(user.id) } returns user
            every { loanRepository.save(any<Loan>()) } answers { firstArg() }

            val result = loanService.create(user.id, book.id)

            result.user shouldBe user
            result.book shouldBe book
            result.issuedDate shouldBe todaysDate
            result.dueDate shouldBe todaysDate.plusDays(LOAN_PERIOD_IN_DAYS.toLong())
            result.returnedDate shouldBe null
        }

        should("not create loan because of limit of loans") {
            every { userService.canLoanBook(user.id) } returns false

            shouldThrow<UserReachedLoanLimitException> { loanService.create(user.id, book.id) }
        }

        should("not loan book because it is already borrowed") {
            every { userService.canLoanBook(user.id) } returns true
            every { bookService.findById(book.id).loans } returns mutableListOf(loan)

            shouldThrow<BookIsNotAvailableException> { loanService.create(user.id, book.id) }
        }

        should("find loan") {
            val loan = buildLoan()
            every { loanRepository.findByIdOrNull(loan.id) } returns loan

            val result = loanService.findById(loan.id)

            result shouldBe loan
        }

        should("not find loan") {
            val loan = buildLoan()
            every { loanRepository.findByIdOrNull(loan.id) } returns null

            shouldThrow<LoanNotFoundException> { loanService.findById(loan.id) }
        }

        should("return book") {
            val book = buildBook(loans = mutableListOf(loan))
            every { bookService.findById(book.id) } returns book
            every { loanRepository.save(loan) } returns loan

            val result = loanService.returnBook(book.id)

            result shouldBe loan
        }

        should("not return book because it is already returned") {
            every { bookService.findById(book.id).loans } returns mutableListOf()

            shouldThrow<BookIsNotBorrowedException> { loanService.returnBook(book.id) }
        }


        should("return null as fine because user delivered book before the due date") {
            val onTimeLoan = buildLoan(dueDate = LocalDate.now().plusDays(1))
            every { loanRepository.save(onTimeLoan) } returns onTimeLoan

            val result = loanService.closeLoan(onTimeLoan)

            result.returnedDate shouldBe LocalDate.now()
            result.fine shouldBe null
        }

        should("return null as fine because user delivered book in the loanedUntil date") {
            val onTimeLoan = buildLoan()
            every { loanRepository.save(onTimeLoan) } returns onTimeLoan

            val result = loanService.closeLoan(onTimeLoan)

            result.returnedDate shouldBe LocalDate.now()
            result.fine shouldBe null
        }

        should("return a fine because user delayed one day") {
            for (delayedDays in 1..60) {
                val overdueLoan = buildLoan(dueDate = LocalDate.now().minusDays(delayedDays.toLong()))
                every { loanRepository.save(overdueLoan) } returns overdueLoan

                val result = loanService.closeLoan(overdueLoan)

                result.returnedDate shouldBe LocalDate.now()
                result.fine?.value shouldBe delayedDays * FINE_IN_REAL_CENTS_PER_DAY
                result.fine?.status shouldBe FineStatus.OPENED
            }
        }

        should("return user loans") {
            val user = buildUser(loans = mutableListOf(loan))
            every { userService.findById(user.id) } returns user

            val result = loanService.findUserLoans(user.id)

            result shouldBe mutableListOf(loan)
        }

        should("pay fine") {
            val overdueLoan = buildLoan(
                    returnedDate = LocalDate.now(),
                    fine = Fine(4.0, FineStatus.OPENED)
            )
            every { loanRepository.findByIdOrNull(overdueLoan.id) } returns overdueLoan
            every { loanRepository.save(overdueLoan) } returns overdueLoan

            val result = loanService.payFine(overdueLoan.id)

            result.fine?.status shouldBe FineStatus.PAID
        }

        should("not pay fine because loan was not closed yet") {
            every { loanRepository.findByIdOrNull(loan.id) } returns loan

            shouldThrow<CouldNotPayFineException> { loanService.payFine(loan.id) }
        }

        should("not pay fine because loan has no fine") {
            val loan = buildLoan(
                    returnedDate = LocalDate.now(),
                    fine = null
            )
            every { loanRepository.findByIdOrNull(loan.id) } returns loan

            shouldThrow<CouldNotPayFineException> { loanService.payFine(loan.id) }
        }

        should("not pay fine because loan fine is already paid") {
            val loan = buildLoan(
                    returnedDate = LocalDate.now(),
                    fine = Fine(4.0, FineStatus.PAID)
            )
            every { loanRepository.findByIdOrNull(loan.id) } returns loan

            shouldThrow<CouldNotPayFineException> { loanService.payFine(loan.id) }
        }
    }

    private fun buildLoan(dueDate: LocalDate = LocalDate.now(),
                          returnedDate: LocalDate? = null,
                          fine: Fine? = null) =
            Loan(
                    user = user,
                    book = book,
                    issuedDate = LocalDate.now(),
                    dueDate = dueDate,
                    returnedDate = returnedDate,
                    fine = fine
            )

    private fun buildBook(loans: MutableList<Loan> = mutableListOf()) = Book(
            title = "Clean Code",
            author = "Uncle Bob",
            loans = loans
    )

    private fun buildUser(loans: MutableList<Loan> = mutableListOf()) = User(
            name = "Bob",
            documentId = "7896521",
            loans = loans
    )

}