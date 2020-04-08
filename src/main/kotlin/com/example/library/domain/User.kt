package com.example.library.domain

import com.fasterxml.jackson.annotation.JsonManagedReference
import java.util.*
import javax.persistence.*

@Entity
@Table(indexes = [Index(columnList = "documentId", unique = true) ])
data class User(
        @Id
        val id: String = UUID.randomUUID().toString().toUpperCase(),
        val name: String,
        val documentId: String,
        @OneToMany(mappedBy = "user")
        @JsonManagedReference
        var loans: MutableList<Loan> = mutableListOf()
)