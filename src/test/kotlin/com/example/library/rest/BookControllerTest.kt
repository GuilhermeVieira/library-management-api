package com.example.library.rest

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class BookControllerTest: BaseControllerTest() {

    @Test
    fun `should create book`() {
        val bookRequest = buildBookRequest()
        mockMvc.perform(post("/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString((bookRequest))))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("title").value(bookRequest.title))
                .andExpect(jsonPath("author").value(bookRequest.author))
                .andExpect(jsonPath("id").isNotEmpty)
    }

    @Test
    fun `should find book`() {
        val book = generateCreatedBook()
        mockMvc.perform(get("/books/${book.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(book.id))
                .andExpect(jsonPath("title").value(book.title))
                .andExpect(jsonPath("author").value(book.author))
    }

    @Test
    fun `should not find book because it does not exist`() {
        mockMvc.perform(get("/books/743879348943eee")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
    }

}