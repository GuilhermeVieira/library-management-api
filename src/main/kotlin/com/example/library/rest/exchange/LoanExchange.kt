package com.example.library.rest.exchange

import com.example.library.domain.Loan
import com.fasterxml.jackson.annotation.JsonProperty

data class LoanExchange (
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("book_id")
        val bookId: String
)

fun Loan.toLoanExchange(): LoanExchange {
        return LoanExchange(
                userId = user.id,
                bookId = book.id
        )
}