package com.example.library.rest

import com.example.library.rest.exchange.LoanExchange
import com.example.library.rest.exchange.toLoanExchange
import com.example.library.service.LoanService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/loans")
class LoanController(val loanService: LoanService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody loan: LoanExchange) =
        loanService.create(loan.userId, loan.bookId).toLoanExchange()

}