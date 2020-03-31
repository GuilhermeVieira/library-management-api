package com.example.library.domain

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Book(
        @Id
        val id: String = UUID.randomUUID().toString().toUpperCase(),
        val title: String,
        val author: String,
        @OneToMany(mappedBy = "book")
        val loans: MutableSet<Loan> = mutableSetOf(),
        val available: Boolean = true
)