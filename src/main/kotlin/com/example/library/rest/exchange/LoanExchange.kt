package com.example.library.rest.exchange

import com.example.library.domain.Fine
import com.example.library.domain.Loan
import java.time.LocalDate

data class LoanExchange (
        var id: String? = null,
        val userId: String,
        val bookId: String,
        var issuedDate: LocalDate? = null,
        var dueDate: LocalDate? = null,
        var returnedDate: LocalDate? = null,
        var fine: Fine? = null
)

fun Loan.toLoanExchange() =
        LoanExchange(
                id = id,
                userId = user.id,
                bookId = book.id,
                issuedDate = issuedDate,
                dueDate = dueDate,
                returnedDate = returnedDate,
                fine = fine
        )

fun List<Loan>.toLoanExchange() = this.map { it.toLoanExchange() }