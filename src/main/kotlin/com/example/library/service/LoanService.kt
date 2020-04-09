package com.example.library.service

import com.example.library.domain.Fine
import com.example.library.domain.FineStatus
import com.example.library.domain.Loan
import com.example.library.exception.BookIsNotAvailableException
import com.example.library.exception.BookIsNotBorrowedException
import com.example.library.exception.UserReachedLoanLimitException
import com.example.library.repository.LoanRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class LoanService(val loanRepository: LoanRepository,
                  val userService: UserService,
                  val bookService: BookService
) {

    val finePerDay = 2.0
    val loanPeriod = 15

    private fun canLoanBook(userId: String) = userService.canLoanBook(userId)

    fun findBookLoans(bookId: String) = bookService.findById(bookId).loans

    fun findBookCurrentLoan(bookId: String) = findBookLoans(bookId).firstOrNull { it.returnedDate == null }

    fun isBookAvailable(bookId: String) = findBookCurrentLoan(bookId) == null

    private fun validateBookLoan(bookId: String) {
        if (!isBookAvailable(bookId)) throw BookIsNotAvailableException()
    }

    private fun validateUserLoan(userId: String) {
        if (!canLoanBook(userId)) throw UserReachedLoanLimitException()
    }

    private fun validateLoan(userId: String, bookId: String) {
        validateUserLoan(userId)
        validateBookLoan(bookId)
    }

    fun createLoanEntity(userId: String, bookId: String) =
        Loan(
                user = userService.findById(userId),
                book = bookService.findById(bookId),
                issuedDate = LocalDate.now(),
                dueDate = LocalDate.now().plusDays(loanPeriod.toLong()),
                returnedDate = null
        )

    fun create(userId: String, bookId: String): Loan {
        validateLoan(userId, bookId)
        return loanRepository.save(createLoanEntity(userId, bookId))
    }

    fun returnBook(bookId: String): Loan {
        val loan = findBookCurrentLoan(bookId) ?: throw BookIsNotBorrowedException()
        loan.returnedDate = LocalDate.now()
        loan.fine = computeFine(loan)
        return loanRepository.save(loan)
    }

    fun computeFine(loan: Loan): Fine? {
        val overdueDays = ChronoUnit.DAYS.between(loan.dueDate, LocalDate.now())

        if (overdueDays > 0) {
            return Fine(overdueDays * finePerDay, FineStatus.OPENED)
        }

        return null
    }

    fun findUserLoans(userId: String) = userService.findById(userId).loans

}
