package com.example.library.rest

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerTest: BaseControllerTest() {

    @Test
    fun `should create user`() {
        val userRequest = buildUserRequest()
        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("name").value(userRequest.name))
                .andExpect(jsonPath("document_id").value(userRequest.documentId))
                .andExpect(jsonPath("id").isNotEmpty)
    }

    @Test
    fun `should not create user because it already exists`() {
        val user = generateCreatedUser()
        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildUserRequest(id = user.documentId))))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `should create both users with different documentId's`() {
        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildUserRequest(id = "8965241"))))
                .andExpect(status().isCreated)

        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildUserRequest(id = "8796521"))))
                .andExpect(status().isCreated)
    }

    @Test
    fun `should find user`() {
        val user = generateCreatedUser()
        mockMvc.perform(get("/users/${user.id}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(user.id))
                .andExpect(jsonPath("name").value(user.name))
                .andExpect(jsonPath("document_id").value(user.documentId))
    }

    @Test
    fun `should not find user because it does not exist`() {
        mockMvc.perform(get("/users/${"98551848715187eee"}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
    }

}