package com.example.library.exception

import java.lang.RuntimeException

class UserNotFoundException(override val message: String = ErrorCode.USER_NOT_FOUND): NotFoundException()

class UserAlreadyExistsException(override val message: String = ErrorCode.USER_ALREADY_EXISTS): BadRequestException()

class UserReachedLoanLimitException(override val message: String = ErrorCode.USER_REACHED_LOAN_LIMIT): ForbiddenException()

class BookNotFoundException(override val message: String = ErrorCode.BOOK_NOT_FOUND): NotFoundException()

class BookIsNotAvailableException(override val message: String = ErrorCode.BOOK_IS_ALREADY_BORROWED): BadRequestException()

class BookIsNotBorrowedException(override val message: String = ErrorCode.BOOK_IS_NOT_BORROWED): BadRequestException()

class LoanNotFoundException(override val message: String = ErrorCode.LOAN_NOT_FOUND): NotFoundException()

class CouldNotPayFineException(override val message: String = ErrorCode.COULD_NOT_PAY_FINE): BadRequestException()

open class BadRequestException: RuntimeException()

open class NotFoundException: RuntimeException()

open class ForbiddenException: RuntimeException()

object ErrorCode {
    const val USER_NOT_FOUND = "User not found"
    const val USER_ALREADY_EXISTS = "There is already an user with the informed document id"
    const val BOOK_NOT_FOUND = "Book not found"
    const val USER_REACHED_LOAN_LIMIT = "User has reached the limit of loans"
    const val BOOK_IS_ALREADY_BORROWED = "Book is already borrowed"
    const val BOOK_IS_NOT_BORROWED = "Book is not currently borrowed"
    const val LOAN_NOT_FOUND = "Loan not found"
    const val COULD_NOT_PAY_FINE = "Could not pay fine because loan was not closed yet or fine is already paid"
}
