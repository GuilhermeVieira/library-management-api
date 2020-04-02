package com.example.library.rest.exchange

import com.example.library.domain.Loan
import java.time.LocalDate

data class LoanExchange (
        var id: String? = null,
        val userId: String,
        val bookId: String,
        var issuedDate: LocalDate? = null,
        var loanedUntil: LocalDate? = null,
        var returnedDate: LocalDate? = null
)

fun Loan.toLoanExchange() =
        LoanExchange(
                id = id,
                userId = user.id,
                bookId = book.id,
                issuedDate = issuedDate,
                loanedUntil = loanedUntil,
                returnedDate = returnedDate
        )