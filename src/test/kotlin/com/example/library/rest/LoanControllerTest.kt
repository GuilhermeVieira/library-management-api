package com.example.library.rest

import org.junit.jupiter.api.Test

import org.springframework.http.MediaType
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
                .andExpect(jsonPath("loaned_until").value(LocalDate.now().toString()))
                .andExpect(jsonPath("returned_date").doesNotExist())
    }

    @Test
    fun `should return book`() {
        val userResponse = createUser("8965216")
        val bookResponse = createBook()
        val loanResponse = createLoan(userResponse.id!!, bookResponse.id!!)

        mockMvc.perform(post("/loans/${bookResponse.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(loanResponse.id!!))
                .andExpect(jsonPath("returned_date").value(LocalDate.now().toString()))
    }

    @Test
    fun `should not return book because it is already returned`() {
        val bookResponse = createBook()
        mockMvc.perform(post("/loans/${bookResponse.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
    }

}