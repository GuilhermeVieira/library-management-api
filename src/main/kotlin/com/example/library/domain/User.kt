package com.example.library.domain

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class User(
        @Id
        val id: String = UUID.randomUUID().toString().toUpperCase(),
        val name: String,
        val documentId: String,
        @OneToMany(mappedBy = "user")
        val loans: Set<Loan> = mutableSetOf()
)