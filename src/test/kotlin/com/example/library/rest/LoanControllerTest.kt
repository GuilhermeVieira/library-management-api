package com.example.library.rest

import org.junit.jupiter.api.Test

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LoanControllerTest: BaseControllerTest() {

    @Test
    fun `should create loan`() {
        val userResponse = createUser("8965215")
        val bookResponse = createBook()

        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildLoanRequest(userResponse.id!!, bookResponse.id!!))))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("user_id").value(userResponse.id!!))
                .andExpect(jsonPath("book_id").value(bookResponse.id!!))
    }

}