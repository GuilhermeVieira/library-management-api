package com.example.library.domain

import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
data class Loan(
        @Id
        val id: String = UUID.randomUUID().toString().toUpperCase(),
        @ManyToOne(fetch = FetchType.LAZY)
        val user: User,
        @ManyToOne(fetch = FetchType.LAZY)
        val book: Book,
        val issuedDate: LocalDate,
        val loanedUntil: LocalDate,
        val returnedDate: LocalDate?
)