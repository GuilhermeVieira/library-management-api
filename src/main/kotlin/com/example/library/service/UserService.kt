package com.example.library.service

import com.example.library.domain.User
import com.example.library.exception.ErrorCode
import com.example.library.exception.UserAlreadyExistsException
import com.example.library.exception.UserNotFoundException
import com.example.library.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {

    val userLoanLimit = 10

    private fun userExists(user: User) =
            userRepository.findByDocumentId(user.documentId) == null

    fun create(user: User) =
        takeIf {
            userExists(user)
        }?.let {
            userRepository.save(user)
        } ?: throw UserAlreadyExistsException(ErrorCode.USER_ALREADY_EXISTS)

    fun findById(id: String) = userRepository.findByIdOrNull(id)
            ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND)

    fun getUserLoansSize(id: String) = findById(id).loans.size

    fun canLoanBook(id: String) = getUserLoansSize(id) < userLoanLimit

}