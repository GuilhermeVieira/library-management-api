package com.example.library.domain

import com.fasterxml.jackson.annotation.JsonBackReference
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
data class Loan(
        @Id
        val id: String = UUID.randomUUID().toString().toUpperCase(),
        @ManyToOne(fetch = FetchType.LAZY)
        @JsonBackReference
        val user: User,
        @ManyToOne(fetch = FetchType.LAZY)
        val book: Book,
        val issuedDate: LocalDate,
        val dueDate: LocalDate,
        var returnedDate: LocalDate?,
        var fine: Fine? = null
)

@Embeddable
data class Fine(
        val value: Double?,
        val status: FineStatus?
)

enum class FineStatus {
        OPENED,
        PAID
}