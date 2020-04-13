package com.example.library.service

import com.example.library.domain.Fine
import com.example.library.domain.FineStatus
import com.example.library.domain.Loan
import com.example.library.exception.*
import com.example.library.repository.LoanRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

const val FINE_PER_DAY = 2.0
const val LOAN_PERIOD = 15

@Service
class LoanService(private val loanRepository: LoanRepository,
                  private val userService: UserService,
                  private val bookService: BookService
) {

    private fun canLoanBook(userId: String) = userService.canLoanBook(userId)

    fun findById(id: String) = loanRepository.findByIdOrNull(id) ?: throw LoanNotFoundException()

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
                    dueDate = LocalDate.now().plusDays(LOAN_PERIOD.toLong()),
                    returnedDate = null
            )

    fun create(userId: String, bookId: String): Loan {
        validateLoan(userId, bookId)
        return loanRepository.save(createLoanEntity(userId, bookId))
    }

    fun returnBook(bookId: String) =
            findBookCurrentLoan(bookId)?.let { close(it) } ?: throw BookIsNotBorrowedException()

    fun close(loan: Loan): Loan {
        val overdueDays = ChronoUnit.DAYS.between(loan.dueDate, LocalDate.now())
        loan.apply {
            returnedDate = LocalDate.now()
            fine = takeIf { overdueDays > 0 }?.let { Fine(overdueDays * FINE_PER_DAY, FineStatus.OPENED) }
        }
        return loanRepository.save(loan)
    }

    fun findUserLoans(userId: String) = userService.findById(userId).loans

    private fun isClosed(loan: Loan) = (loan.returnedDate != null)

    private fun hasFine(loan: Loan) = (loan.fine?.status == FineStatus.OPENED)

    private fun isFineOpened(loan: Loan) = (isClosed(loan) && hasFine(loan))

    fun payFine(loanId: String) =
            findById(loanId).takeIf {
                isFineOpened(it)
            }?.let {
                loanRepository.save(it.apply { fine?.status = FineStatus.PAID })
            } ?: throw CouldNotPayFineException()
}
