package com.example.library.rest.exchange

import com.example.library.domain.Loan

data class LoanExchange (
        val userId: String,
        val bookId: String
)

fun Loan.toLoanExchange() = LoanExchange(userId = user.id, bookId = book.id)