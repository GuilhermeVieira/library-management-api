package com.example.library.exception

object ErrorCode {
    const val USER_NOT_FOUND = "User not found"
    const val USER_ALREADY_EXISTS = "There is already an user with the informed document id"
    const val BOOK_NOT_FOUND = "Book not found"
    const val USER_REACHED_LOAN_LIMIT = "User has reached the limit of loans"
    const val BOOK_IS_ALREADY_BORROWED = "Book is already borrowed"
}