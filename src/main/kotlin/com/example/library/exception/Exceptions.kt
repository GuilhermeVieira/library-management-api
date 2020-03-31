package com.example.library.exception

import java.lang.RuntimeException

class UserNotFoundException(override val message: String): NotFoundException()

class UserAlreadyExistsException(override val message: String): BadRequestException()

class UserReachedLoanLimitException(override val message: String): BadRequestException()

class BookNotFoundException(override val message: String): NotFoundException()

class BookIsNotAvailableException(override val message: String): BadRequestException()

open class BadRequestException: RuntimeException()

open class NotFoundException: RuntimeException()
