package com.example.library.rest

import com.example.library.rest.exchange.UserExchange
import com.example.library.rest.exchange.toDomain
import com.example.library.rest.exchange.toUserExchange
import com.example.library.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(val userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody user: UserExchange): UserExchange {
        return userService.create(user.toDomain()).toUserExchange()
    }

    @GetMapping("/{id}")
    fun findUser(@PathVariable id: String): UserExchange {
        return userService.findById(id).toUserExchange()
    }

}