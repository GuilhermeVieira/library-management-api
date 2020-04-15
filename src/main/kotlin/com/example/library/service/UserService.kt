package com.example.library.service

import com.example.library.domain.User
import com.example.library.exception.UserAlreadyExistsException
import com.example.library.exception.UserNotFoundException
import com.example.library.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

const val USER_LOAN_LIMIT = 10

@Service
class UserService(private val userRepository: UserRepository) {

    fun create(user: User) =
        takeIf {
            !userExists(user)
        }?.let {
            userRepository.save(user)
        } ?: throw UserAlreadyExistsException()

    fun findById(id: String) = userRepository.findByIdOrNull(id)
            ?: throw UserNotFoundException()

    fun getUserLoansSize(id: String) = findById(id).loans.size

    fun canLoanBook(id: String) = getUserLoansSize(id) < USER_LOAN_LIMIT

    private fun userExists(user: User) =
            userRepository.findByDocumentId(user.documentId) != null

}