package com.example.library.service

import com.example.library.domain.Loan
import com.example.library.exception.BookIsNotAvailableException
import com.example.library.exception.ErrorCode
import com.example.library.exception.UserReachedLoanLimitException
import com.example.library.repository.LoanRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LoanService(val loanRepository: LoanRepository,
                  val userService: UserService,
                  val bookService: BookService
) {

    private fun canLoanBook(userId: String): Boolean {
        return userService.canLoanBook(userId)
    }

    private fun validateUserLoan(userId: String) {
        if (!canLoanBook(userId)) {
            throw UserReachedLoanLimitException(ErrorCode.USER_REACHED_LOAN_LIMIT)
        }
    }

    private fun validateBookLoan(bookId: String) {
        if (!bookService.isAvailable(bookId)) {
            throw BookIsNotAvailableException(ErrorCode.BOOK_IS_ALREADY_BORROWED)
        }
    }

    private fun validateLoan(userId: String, bookId: String) {
        validateUserLoan(userId)
        validateBookLoan(bookId)
    }

    fun createLoanEntity(userId: String, bookId: String): Loan {
        return Loan(
                user = userService.findById(userId),
                book = bookService.findById(bookId),
                issuedDate = LocalDate.now(),
                loanedUntil = LocalDate.now(),
                returnedDate = null
        )
    }

    fun create(userId: String, bookId: String): Loan {
        validateLoan(userId, bookId)
        return loanRepository.save(createLoanEntity(userId, bookId))
    }

}