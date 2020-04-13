package com.example.library.service

import com.example.library.domain.User
import com.example.library.exception.UserAlreadyExistsException
import com.example.library.exception.UserNotFoundException
import com.example.library.repository.UserRepository
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull

class UserServiceTest : ShouldSpec() {

    private val userRepository = mockk<UserRepository>()

    private val userService = UserService(userRepository)

    private val user = User(
            name = "Bob",
            documentId = "7896321"
    )

    init {
        should("create user") {
            every { userRepository.findByDocumentId(user.documentId) } returns null
            every { userRepository.save(user) } returns user

            val result = userService.create(user)

            result shouldBe user
        }

        should("not create user when user already exists") {
            every { userRepository.findByDocumentId(user.documentId) } returns user

            shouldThrow<UserAlreadyExistsException> { userService.create(user) }
        }

        should("find user by id if it exists") {
            every { userRepository.findByIdOrNull(user.id) } returns user

            val result = userService.findById(user.id)

            result shouldBe user
        }

        should("not find user by id if it does not exist") {
            every { userRepository.findByIdOrNull(user.id) } returns null

            shouldThrow<UserNotFoundException> { userService.findById(user.id) }
        }

        should("return true because user has not reached the loan limit") {
            every { userService.getUserLoansSize(user.id) } returns userService.userLoanLimit - 1

            val result = userService.canLoanBook(user.id)

            result shouldBe true
        }

        should("return false because user has reached the loan limit") {
            every { userService.getUserLoansSize(user.id) } returns userService.userLoanLimit

            val result = userService.canLoanBook(user.id)

            result shouldBe false
        }
    }
}