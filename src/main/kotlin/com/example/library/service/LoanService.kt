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

const val FINE_IN_REAL_CENTS_PER_DAY = 200.00
const val LOAN_PERIOD_IN_DAYS = 15

@Service
class LoanService(private val loanRepository: LoanRepository,
                  private val userService: UserService,
                  private val bookService: BookService
) {

    private  fun createLoanEntity(userId: String, bookId: String) =
            Loan(
                    user = userService.findById(userId),
                    book = bookService.findById(bookId),
                    issuedDate = LocalDate.now(),
                    dueDate = LocalDate.now().plusDays(LOAN_PERIOD_IN_DAYS.toLong()),
                    returnedDate = null
            )

    fun create(userId: String, bookId: String): Loan {
        validateLoan(userId, bookId)
        return loanRepository.save(createLoanEntity(userId, bookId))
    }

    fun findById(id: String) = loanRepository.findByIdOrNull(id) ?: throw LoanNotFoundException()

    fun returnBook(bookId: String) =
            findBookCurrentLoan(bookId)?.let { closeLoan(it) } ?: throw BookIsNotBorrowedException()

    fun closeLoan(loan: Loan): Loan {
        val overdueDays = ChronoUnit.DAYS.between(loan.dueDate, LocalDate.now())
        loan.apply {
            returnedDate = LocalDate.now()
            fine = takeIf { overdueDays > 0 }?.let { Fine(overdueDays * FINE_IN_REAL_CENTS_PER_DAY, FineStatus.OPENED) }
        }
        return loanRepository.save(loan)
    }

    fun findUserLoans(userId: String) = userService.findById(userId).loans

    fun payFine(loanId: String) =
            findById(loanId).takeIf {
                isFineOpened(it)
            }?.let {
                loanRepository.save(it.apply { fine?.status = FineStatus.PAID })
            } ?: throw CouldNotPayFineException()

    private fun findBookLoans(bookId: String) = bookService.findById(bookId).loans

    private fun findBookCurrentLoan(bookId: String) = findBookLoans(bookId).firstOrNull { it.returnedDate == null }

    private fun isBookAvailable(bookId: String) = findBookCurrentLoan(bookId) == null

    private fun canLoanBook(userId: String) = userService.canLoanBook(userId)

    private fun isClosed(loan: Loan) = (loan.returnedDate != null)

    private fun hasFine(loan: Loan) = (loan.fine?.status == FineStatus.OPENED)

    private fun isFineOpened(loan: Loan) = (isClosed(loan) && hasFine(loan))

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
}
