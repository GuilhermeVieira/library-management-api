package com.example.library.rest

import com.example.library.rest.exchange.LoanExchange
import com.example.library.rest.exchange.toLoanExchange
import com.example.library.service.LoanService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/loans")
class LoanController(private val loanService: LoanService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody loan: LoanExchange) =
        loanService.create(loan.userId, loan.bookId).toLoanExchange()

    @PostMapping("/{bookId}")
    fun returnBook(@PathVariable bookId: String) =
            loanService.returnBook(bookId).toLoanExchange()

    @GetMapping("/users/{userId}")
    fun findUserLoans(@PathVariable userId: String) =
            loanService.findUserLoans(userId).toLoanExchange()

    @PostMapping("/payments/{loanId}")
    fun payFine(@PathVariable loanId: String) =
            loanService.payFine(loanId).toLoanExchange()

}