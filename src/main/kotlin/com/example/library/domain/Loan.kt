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
        var returnedDate: LocalDate?,
        var fine: Fine = Fine()
)

@Embeddable
data class Fine(
        var fineValue: Double = 0.0,
        var fineStatus: FineStatus = FineStatus.NOT_CHARGED
)

enum class FineStatus {
        NOT_CHARGED,
        OPENED,
        PAID
}