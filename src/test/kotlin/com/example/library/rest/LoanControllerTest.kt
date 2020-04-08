package com.example.library.rest

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class LoanControllerTest: BaseControllerTest() {

    @Test
    fun `should create loan`() {
        val userResponse = createUser("8965215")
        val bookResponse = createBook()

        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildLoanRequest(userResponse.id!!, bookResponse.id!!))))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("id").isNotEmpty)
                .andExpect(jsonPath("user_id").value(userResponse.id!!))
                .andExpect(jsonPath("book_id").value(bookResponse.id!!))
                .andExpect(jsonPath("issued_date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("due_date").value(LocalDate.now().plusDays(loanService.loanPeriod.toLong()).toString()))
                .andExpect(jsonPath("returned_date").doesNotExist())
    }

    @Test
    fun `should return book with no fine`() {
        val userResponse = createUser("8965216")
        val bookResponse = createBook()
        val loanResponse = createLoan(userResponse.id!!, bookResponse.id!!)

        mockMvc.perform(post("/loans/${bookResponse.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(loanResponse.id!!))
                .andExpect(jsonPath("returned_date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.fine").doesNotExist())
    }

    @Test
    fun `should not return book because it is already returned`() {
        val bookResponse = createBook()
        mockMvc.perform(post("/loans/${bookResponse.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return book and loan it again to another user`() {
        val userResponse = createUser("8965217")
        val otherUserResponse = createUser("8965218")
        val bookResponse = createBook()
        createLoan(userResponse.id!!, bookResponse.id!!)
        returnBook(bookResponse.id!!)

        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildLoanRequest(otherUserResponse.id!!, bookResponse.id!!))))
                .andExpect(status().isCreated)
    }

    @Test
    fun `should not create loan because book is not available`() {
        val userResponse = createUser("8965219")
        val bookResponse = createBook()
        val otherUserResponse = createUser("8965220")
        createLoan(userResponse.id!!, bookResponse.id!!)

        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildLoanRequest(otherUserResponse.id!!, bookResponse.id!!))))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return user loans`() {
        val userResponse = createUser("8965221")
        val bookResponse = createBook()
        createLoan(userResponse.id!!, bookResponse.id!!)

        mockMvc.perform(get("/loans/users/${userResponse.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.[0].id").isNotEmpty)
                .andExpect(jsonPath("$.[0].user_id").value(userResponse.id!!))
                .andExpect(jsonPath("$.[0].book_id").value(bookResponse.id!!))
    }

    @Test
    fun `should return an empty list as user loans`() {
        val userResponse = createUser("8965222")

        mockMvc.perform(get("/loans/users/${userResponse.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `should not return user loans because user does not exist`() {
        mockMvc.perform(get("/loans/users/notExistingUser8932"))
                .andExpect(status().isNotFound)
    }

}