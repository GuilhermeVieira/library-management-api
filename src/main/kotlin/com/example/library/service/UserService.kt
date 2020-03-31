package com.example.library.service

import com.example.library.config.ApplicationConfiguration
import com.example.library.domain.User
import com.example.library.exception.ErrorCode
import com.example.library.exception.UserAlreadyExistsException
import com.example.library.exception.UserNotFoundException
import com.example.library.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {

    private fun documentIdExists(documentId: String): Boolean {
        if (userRepository.findByDocumentId(documentId) == null) {
            return false
        }
        return true
    }

    fun create(user: User): User {
        if (documentIdExists(user.documentId)) {
            throw UserAlreadyExistsException(ErrorCode.USER_ALREADY_EXISTS)
        }
        return userRepository.save(user)
    }

    fun findById(id: String) = userRepository.findByIdOrNull(id)
            ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND)

    // Untested
    fun getUserLoansSize(id: String): Int = findById(id).loans.size

    fun canLoanBook(id: String): Boolean {
        if (getUserLoansSize(id) < ApplicationConfiguration.USER_LOAN_LIMIT) {
            return true
        }
        return false
    }

}