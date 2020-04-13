package com.example.library.rest

import com.example.library.domain.FineStatus
import com.example.library.exception.ErrorCode
import com.example.library.service.FINE_PER_DAY
import com.example.library.service.LOAN_PERIOD
import com.example.library.service.USER_LOAN_LIMIT
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.*

class LoanControllerTest : BaseControllerTest() {

    @Test
    fun `should create loan`() {
        val user = generateCreatedUser()
        val book = generateCreatedBook()

        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(user.id, book.id))))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("id").isNotEmpty)
                .andExpect(jsonPath("user_id").value(user.id))
                .andExpect(jsonPath("book_id").value(book.id))
                .andExpect(jsonPath("issued_date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("due_date").value(LocalDate.now().plusDays(LOAN_PERIOD.toLong()).toString()))
                .andExpect(jsonPath("returned_date").doesNotExist())
    }

    @Test
    fun `should create loan because user has not exceeded its loan limit`() {
        val user = generateCreatedUser()
        user.generateLoans(USER_LOAN_LIMIT - 1)
        val book = generateCreatedBook()

        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(user.id, book.id))))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("id").isNotEmpty)
                .andExpect(jsonPath("user_id").value(user.id))
                .andExpect(jsonPath("book_id").value(book.id))
                .andExpect(jsonPath("issued_date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("due_date").value(LocalDate.now().plusDays(LOAN_PERIOD.toLong()).toString()))
                .andExpect(jsonPath("returned_date").doesNotExist())
    }

    @Test
    fun `should not create loan because user exceeded its loan limit`() {
        val user = generateCreatedUser()
        user.generateLoans(USER_LOAN_LIMIT)
        val book = generateCreatedBook()

        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(user.id, book.id))))
                .andExpect(status().isForbidden)
                .andExpect(content().string(ErrorCode.USER_REACHED_LOAN_LIMIT))
    }

    @Test
    fun `should not create loan because book is not available`() {
        val user = generateCreatedUser()
        val book = generateCreatedBook()
        val otherUser = generateCreatedUser()
        saveLoan(createBaseLoan(user, book))

        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(otherUser.id, book.id))))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(ErrorCode.BOOK_IS_ALREADY_BORROWED))
    }

    @Test
    fun `should not create loan because user does not exist`() {
        val book = generateCreatedBook()
        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(UUID.randomUUID().toString(), book.id))))
                .andExpect(status().isNotFound)
                .andExpect(content().string(ErrorCode.USER_NOT_FOUND))
    }

    @Test
    fun `should not create loan because book does not exist`() {
        val user = generateCreatedUser()
        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(user.id, UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound)
                .andExpect(content().string(ErrorCode.BOOK_NOT_FOUND))
    }

    @Test
    fun `should not create loan because book is already loaned to the same user`() {
        val user = generateCreatedUser()
        val book = generateCreatedBook()
        saveLoan(createBaseLoan(user, book))

        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(user.id, book.id))))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(ErrorCode.BOOK_IS_ALREADY_BORROWED))
    }

    @Test
    fun `should return book with no fine`() {
        val user = generateCreatedUser()
        val book = generateCreatedBook()
        val loan = saveLoan(createBaseLoan(user = user, book = book))

        mockMvc.perform(post("/loans/${book.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(loan.id))
                .andExpect(jsonPath("returned_date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.fine").doesNotExist())
    }

    @Test
    fun `should return book with fine`() {
        val overdueDays = 27.toLong()
        val user = generateCreatedUser()
        val book = generateCreatedBook()
        val loan = saveLoan(createBaseLoan(user = user, book = book, dueDate = LocalDate.now().minusDays(overdueDays)))

        mockMvc.perform(post("/loans/${book.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(loan.id))
                .andExpect(jsonPath("returned_date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.fine.value").value(FINE_PER_DAY * overdueDays))
                .andExpect(jsonPath("$.fine.status").value(FineStatus.OPENED.toString()))
    }

    @Test
    fun `should not return book because it is already returned`() {
        val book = generateCreatedBook()
        mockMvc.perform(post("/loans/${book.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(ErrorCode.BOOK_IS_NOT_BORROWED))
    }

    @Test
    fun `should loan returned book`() {
        val user = generateCreatedUser()
        val otherUser = generateCreatedUser()
        val book = generateCreatedBook()
        val loan = saveLoan(createBaseLoan(user, book))
        saveLoan(loan.apply {
            returnedDate = LocalDate.now()
        })

        mockMvc.perform(post("/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildLoanRequest(otherUser.id, book.id))))
                .andExpect(status().isCreated)
    }

    @Test
    fun `should return one user loan`() {
        val user = generateCreatedUser()
        val book = generateCreatedBook()
        saveLoan(createBaseLoan(user, book))

        mockMvc.perform(get("/loans/users/${user.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.[0].id").isNotEmpty)
                .andExpect(jsonPath("$.[0].user_id").value(user.id))
                .andExpect(jsonPath("$.[0].book_id").value(book.id))
    }

    @Test
    fun `should return multiple user loans`() {
        val n = 5
        val user = generateCreatedUser()
        user.generateLoans(n)

        mockMvc.perform(get("/loans/users/${user.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(n))
    }

    @Test
    fun `should return an empty list as user loans`() {
        val user = generateCreatedUser()

        mockMvc.perform(get("/loans/users/${user.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `should not return user loans because user does not exist`() {
        mockMvc.perform(get("/loans/users/notExistingUser8932"))
                .andExpect(status().isNotFound)
                .andExpect(content().string(ErrorCode.USER_NOT_FOUND))
    }

    @Test
    fun `should pay fine`() {
        val fineValue = 40.0
        val loan = saveLoan(createBaseLoan().close(applyFine = true, fineValue = fineValue))

        mockMvc.perform(post("/loans/payments/${loan.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").value(loan.id))
                .andExpect(jsonPath("fine.value").value(fineValue))
                .andExpect(jsonPath("fine.status").value(FineStatus.PAID.toString()))
    }

    @Test
    fun `should not pay fine because loan was not closed`() {
        val loan = saveLoan(createBaseLoan())
        mockMvc.perform(post("/loans/payments/${loan.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(ErrorCode.COULD_NOT_PAY_FINE))
    }

    @Test
    fun `should not pay fine because loan fine has no fine`() {
        val loan = saveLoan(createBaseLoan().close(applyFine = false))
        mockMvc.perform(post("/loans/payments/${loan.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(ErrorCode.COULD_NOT_PAY_FINE))
    }

    @Test
    fun `should not pay fine because fine is already paid`() {
        val loan = saveLoan(createBaseLoan().close(applyFine = false, paid = true))
        mockMvc.perform(post("/loans/payments/${loan.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(ErrorCode.COULD_NOT_PAY_FINE))
    }

}