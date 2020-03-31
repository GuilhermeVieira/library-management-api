package com.example.library.repository

import com.example.library.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, String> {

    fun findByDocumentId(documentID: String): User?

}